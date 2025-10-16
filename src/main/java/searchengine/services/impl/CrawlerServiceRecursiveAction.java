package searchengine.services.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import searchengine.config.CrawlerSettings;
import searchengine.exceptions.CrawlerServiceCancelException;
import searchengine.exceptions.CrawlerServiceException;
import searchengine.model.IndexingStatus;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.TaskSleepBlocker;
import searchengine.utils.URLUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CrawlerServiceRecursiveAction extends RecursiveAction {
    public static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);

    private final Site site;
    private String path;
    private final Set<String> visitedPaths;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final CrawlerSettings crawlerSettings;
    public final AtomicBoolean isCanceled;
    private final String baseUrl;
    private final String entryPointPath;
    private volatile boolean isRootTask = false;
    private int reconnectCounter = 0;

    public CrawlerServiceRecursiveAction(Site site,
                                         SiteRepository siteRepository,
                                         PageRepository pageRepository,
                                         CrawlerSettings crawlerSettings) {
        this(
                site,
                "/",
                ConcurrentHashMap.newKeySet(),
                siteRepository,
                pageRepository,
                crawlerSettings,
                new AtomicBoolean(false)
        );
        this.isRootTask = true;
    }

    private CrawlerServiceRecursiveAction(Site site,
                                         String path,
                                         Set<String> visitedPaths,
                                         SiteRepository siteRepository,
                                         PageRepository pageRepository,
                                         CrawlerSettings crawlerSettings,
                                         AtomicBoolean isCanceled) {
        this.site = site;
        this.path = path;
        this.visitedPaths = visitedPaths;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.crawlerSettings = crawlerSettings;
        this.isCanceled = isCanceled;
        this.baseUrl = URLUtils.extractBaseUrl(site.getUrl());
        this.entryPointPath = URLUtils.extractEntryPointPath(site.getUrl());
        INSTANCE_COUNTER.incrementAndGet();
    }

    @Override
    protected void compute() {
        if (isRootTask) {
            try {
                Page rootPage = fetch(site.getUrl());
                path = entryPointPath;

                if (rootPage.getCode() != 200) {
                    updateSiteStatus("Сайт недоступен. Код ответа: " + rootPage.getCode(),
                            IndexingStatus.FAILED);
                    INSTANCE_COUNTER.decrementAndGet();
                    return;
                }

                savePage(rootPage);
            } catch (CrawlerServiceException | CrawlerServiceCancelException e) {
                updateSiteStatus(e.getMessage(), IndexingStatus.FAILED);
                INSTANCE_COUNTER.decrementAndGet();
                return;
            }
        }

        String htmlContent = getHtmlContent(path);

        if (htmlContent.isEmpty()) {
            INSTANCE_COUNTER.decrementAndGet();
            return;
        }

        List<String> pathList = new ArrayList<>();
        extractFilteredRelativePaths(htmlContent, pathList);

        if (pathList.isEmpty()) {
            if (isRootTask) updateSiteStatus(IndexingStatus.INDEXED);
            INSTANCE_COUNTER.decrementAndGet();
            return;
        }

        crawlPathBatch(pathList);
        pathList.clear();

        if (isCanceled.get()) {
            if (isRootTask) updateSiteStatus("Индексация отменена пользователем", IndexingStatus.FAILED);
            INSTANCE_COUNTER.decrementAndGet();
            return;
        }

        if (isRootTask) updateSiteStatus(IndexingStatus.INDEXED);
        INSTANCE_COUNTER.decrementAndGet();
    }

    public void cancelAll() {
        isCanceled.set(true);
    }

    private Page fetch(String url) throws CrawlerServiceException {
        if (isCanceled.get()) throw new CrawlerServiceCancelException("Индексация отменена пользователем");

        timeout(reconnectCounter > 0 ?
                crawlerSettings.getReconnectAttemptTimeoutMillis() :
                crawlerSettings.getRandomConnectionTimeoutMillis()
        );

        try {
            Connection connection = Jsoup.connect(url)
                    .headers(crawlerSettings.getRotatingCrawlerHeaders())
                    .timeout(crawlerSettings.getPageLoadTimeLimitMillis());
            Response response = connection.execute();

            if (!response.contentType().contains("text/html;"))
                throw new CrawlerServiceException(url, "Content type does not match 'text/html'");

            return preparePageEntity(
                    URLUtils.toRelativePath(url, baseUrl),
                    response.statusCode(),
                    connection.get().html()
            );
        } catch (SSLHandshakeException e) {
            throw new CrawlerServiceException(url, "SSL error. Unable to establish safe connection.");
        } catch (HttpStatusException e) {
            return preparePageEntity(
                    URLUtils.toRelativePath(e.getUrl(), baseUrl),
                    e.getStatusCode(),
                    "");
        } catch (SocketException | SocketTimeoutException | UnknownHostException e) {
            if (reconnectCounter > crawlerSettings.getReconnectAttemptsMax()) {
                reconnectCounter = 0;
                throw new CrawlerServiceException(url, e.getMessage());
            }
            reconnectCounter++;
            Page page = fetch(url);
            reconnectCounter = 0;
            return page;
        } catch (IOException | IllegalArgumentException e) {
            throw new CrawlerServiceException(url, e.getMessage());
        }
    }

    private void extractFilteredRelativePaths(String htmlContent, List<String> collector) {
        Elements anchors = Jsoup.parse(htmlContent, baseUrl).getElementsByTag("a");

        if (anchors.isEmpty()) return;

        anchors.stream()
                .map((anchor) -> anchor.attr("href"))
                .filter(url -> URLUtils.isValidLink(url, baseUrl))
                .map(url -> URLUtils.toRelativePath(url, baseUrl))
                .filter(path -> path.startsWith(entryPointPath))
                .filter(visitedPaths::add)
                .forEach(collector::add);
    }

    private void crawlPathBatch(List<String> pathList) {
        int bufferSize = computeDynamicBufferSize(pathList.size());

        for (int skip = 0; skip < bufferSize;) {
            int remainingItems = pathList.size() - skip;
            int limit = Math.min(remainingItems, bufferSize);

            List<Page> pageList = new ArrayList<>(limit);
            List<CrawlerServiceRecursiveAction> subTasks = new ArrayList<>(limit);

            pathList.stream().skip(skip).limit(limit).forEach(path -> {
                try {
                    pageList.add(fetch(baseUrl + path));
                    subTasks.add(
                            new CrawlerServiceRecursiveAction(site, path, visitedPaths, siteRepository, pageRepository, crawlerSettings, isCanceled)
                    );
                } catch (CrawlerServiceException e) {
                    updateSiteLastErrorMessage(e.getMessage());
                } catch (CrawlerServiceCancelException e) {}
            });

            saveAllPages(pageList);
            pageList.clear();

            if (isCanceled.get()) {
                INSTANCE_COUNTER.set(INSTANCE_COUNTER.get() - subTasks.size());
                subTasks.clear();
                return;
            }

            subTasks.forEach(ForkJoinTask::fork);
            subTasks.forEach(ForkJoinTask::join);
            subTasks.clear();

            skip += limit;
        }
    }

    @Transactional
    public void savePage(Page page) {
        pageRepository.saveOnDuplicateKeyUpdateCode(page);
        siteRepository.updateStatusTime(site.getId());
    }

    @Transactional
    public void saveAllPages(List<Page> pages) {
        pages.forEach(pageRepository::saveOnDuplicateKeyUpdateCode);
        siteRepository.updateStatusTime(site.getId());
    }

    public String getHtmlContent(String path) {
        return pageRepository.getPageContentBySiteIdAndPath(site.getId(), path).orElse("");
    }

    public void updateSiteLastErrorMessage(String lastErrorMessage) {
        siteRepository.updateLastErrorAndStatusTime(site.getId(), lastErrorMessage);
    }

    public void updateSiteStatus(IndexingStatus status) {
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    public void updateSiteStatus(String lastErrorMessage, IndexingStatus status) {
        site.setLastError(lastErrorMessage);
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    private Page preparePageEntity(String path, int statusCode, String content) {
        Page page = new Page();
        page.setSiteId(site.getId());
        page.setCode(statusCode);
        page.setContent(content);
        page.setPath(path);
        return page;
    }

    private void timeout(int millis) {
        TaskSleepBlocker.safeSleep(millis);
    }

    private int computeDynamicBufferSize(int itemsCount) {
        int storageBatchLimit = crawlerSettings.getStorageBatchLimit();
        return itemsCount < storageBatchLimit ? itemsCount : itemsCount / INSTANCE_COUNTER.get();
    }
}