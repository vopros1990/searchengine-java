package searchengine.services.indexing.tasks;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import searchengine.common.task.TaskSleepBlocker;
import searchengine.common.text.HtmlUtils;
import searchengine.exceptions.IndexingServiceException;
import searchengine.model.*;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.crawler.PageFetcher;
import searchengine.services.indexing.service.LemmaExtractorService;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.common.text.URLUtils;
import searchengine.services.mapper.LemmaIndexMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class SiteIndexingTask extends RecursiveTask<IndexingStatus> {
    private boolean isRootTask = false;
    public final AtomicBoolean isIndexingStopped;
    private short depth;

    private final Site site;
    private final String baseUrl, entryPointPath;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageFetcher pageFetcher;
    private final LemmaExtractorService lemmaExtractorService;

    private final Set<String> visitedPaths;
    private final List<String> pathsToProcess;

    private static final int MAX_DEPTH = 100;
    private static final int MAX_PAGES_PER_TASK = 10;
    private static final int SAVE_RETRY_MAX_ATTEMPTS = 5;
    private static final int SAVE_RETRY_DELAY_MILLIS = 50;


    protected SiteIndexingTask(Site site,
                               SiteRepository siteRepository,
                               PageRepository pageRepository,
                               PageFetcher pageFetcher,
                               LemmaExtractorServiceImpl morphologyService) {
        this(
                site,
                new ArrayList<>(),
                ConcurrentHashMap.newKeySet(),
                siteRepository,
                pageRepository,
                pageFetcher,
                morphologyService,
                new AtomicBoolean(false)
        );

        this.isRootTask = true;
        this.setForkJoinTaskTag((short) 1);
    }

    private SiteIndexingTask(Site site,
                             List<String> pathsToProcess,
                             Set<String> visitedPaths,
                             SiteRepository siteRepository,
                             PageRepository pageRepository,
                             PageFetcher pageFetcher,
                             LemmaExtractorService lemmaExtractorService,
                             AtomicBoolean isIndexingStopped) {
        this.site = site;
        this.pathsToProcess = pathsToProcess;
        this.visitedPaths = visitedPaths;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.pageFetcher = pageFetcher;
        this.lemmaExtractorService = lemmaExtractorService;
        this.isIndexingStopped = isIndexingStopped;
        this.baseUrl = URLUtils.extractBaseUrl(site.getUrl());
        this.entryPointPath = URLUtils.extractEntryPointPath(site.getUrl());
    }

    private SiteIndexingTask createSubTask(List<String> pathsToProcess) {
        return new SiteIndexingTask(
                site,
                pathsToProcess,
                visitedPaths,
                siteRepository,
                pageRepository,
                pageFetcher,
                lemmaExtractorService,
                isIndexingStopped
        );
    }

    @Override
    protected IndexingStatus compute() {
        if ((this.depth = this.getForkJoinTaskTag()) > MAX_DEPTH ||
                isIndexingStopped.get() ||
                !processRootPage())
            return IndexingStatus.FAILED;

        processPaths();
        return IndexingStatus.INDEXED;
    }

    public void cancelAll() {
        isIndexingStopped.set(true);
    }

    private boolean processRootPage() {
        if (!isRootTask) return true;

        try {
            Page page = pageFetcher.fetchPage(baseUrl + entryPointPath);
            page.setSite(site);
            page.setPath("/");

            visitedPaths.add(entryPointPath);

            if (page.getCode() != 200) {
                siteRepository.updateLastErrorAndStatusTime(
                        site.getId(),
                        "Ошибка индексации: главная страница сайта не доступна. Код ответа: " + page.getCode()
                );

                return false;
            }

            extractFilteredRelativePaths(page.getContent(), pathsToProcess);
            processPageLemmas(page);
            savePageWithRetry(page);
            siteRepository.updateStatusTime(site.getId());

            return true;
        } catch (IndexingServiceException e) {
            siteRepository.updateLastErrorAndStatusTime(site.getId(), e.getMessage());
            return false;
        }
    }

    private boolean extractFilteredRelativePaths(String htmlContent, List<String> collector) {
        if(htmlContent.isEmpty()) return false;

        Elements anchors = Jsoup.parse(htmlContent, baseUrl).getElementsByTag("a");
        anchors.stream()
                .map((anchor) -> anchor.attr("href"))
                .filter(url -> URLUtils.isCrawlableUrl(url, baseUrl))
                .map(url -> URLUtils.toRelativePath(url, baseUrl))
                .filter(path -> path.startsWith(entryPointPath))
                .collect(Collectors.toSet()).stream()
                .filter(visitedPaths::add)
                .forEach(collector::add);

        return !anchors.isEmpty();
    }

    private void processPaths() {
        if (pathsToProcess.size() > MAX_PAGES_PER_TASK) {
            divideProcessingPathList();
            return;
        }

        List<ForkJoinTask<IndexingStatus>> subTasks = new ArrayList<>();

        pathsToProcess.forEach(path -> {
            if (isIndexingStopped.get()) this.complete(IndexingStatus.FAILED);

            try {
                Page page = pageFetcher.fetchPage(baseUrl + path);
                page.setSite(site);
                page.setPath(page.getPath().replaceAll("^" + entryPointPath, "/"));

                List<String> extractedPaths = new ArrayList<>();
                if(!extractFilteredRelativePaths(page.getContent(), extractedPaths))
                    return;

                SiteIndexingTask subTask = createSubTask(extractedPaths);
                subTask.setForkJoinTaskTag((short) (depth + 1));
                subTasks.add(subTask.fork());

                processPageLemmas(page);
                savePageWithRetry(page);
                siteRepository.updateStatusTime(site.getId());
            } catch (IndexingServiceException e) {
                if (isIndexingStopped.get()) this.complete(IndexingStatus.FAILED);
                siteRepository.updateLastErrorAndStatusTime(site.getId(), e.getMessage());
            }
        });

        subTasks.forEach(ForkJoinTask::join);
    }

    private void divideProcessingPathList() {
        int splitIndex = pathsToProcess.size() / 2;

        List<String> leftBranch = pathsToProcess.subList(0, splitIndex);
        List<String> rightBranch = pathsToProcess.subList(splitIndex, pathsToProcess.size());

        SiteIndexingTask leftSubTask = createSubTask(leftBranch);
        SiteIndexingTask rightSubTask = createSubTask(rightBranch);

        leftSubTask.setForkJoinTaskTag(this.depth);
        rightSubTask.setForkJoinTaskTag(this.depth);

        leftSubTask.fork();
        rightSubTask.fork();

        leftSubTask.join();
        rightSubTask.join();
    }

    private void processPageLemmas(Page page) {
        Map<String, Integer> lemmasFrequencyMap =
                lemmaExtractorService.buildLemmaFrequencyMap(
                        HtmlUtils.stripHtmlTags(page.getContent())
                );
        LemmaIndexMapper.mapToEntities(page, site, lemmasFrequencyMap);
    }

    @Transactional
    public void savePageWithRetry(Page page) {
        for (int i = 0; i < SAVE_RETRY_MAX_ATTEMPTS; i++) {
            try {
                pageRepository.save(page);
                return;
            } catch (Exception e) {
                log.info(e.getCause().toString());
                TaskSleepBlocker.safeSleep(SAVE_RETRY_DELAY_MILLIS);
            }
        }
    }
}