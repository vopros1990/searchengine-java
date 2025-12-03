package searchengine.services.indexing.tasks;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Map;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class SinglePageIndexingTask extends RecursiveTask<IndexingStatus> {
    private final Site site;
    private final String entryPointPath;
    private String url;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageFetcher pageFetcher;
    private final LemmaExtractorService lemmaExtractorService;

    private static final int SAVE_RETRY_MAX_ATTEMPTS = 5;
    private static final int SAVE_RETRY_DELAY_MILLIS = 50;

    public SinglePageIndexingTask(
            Site site,
            String url,
            SiteRepository siteRepository,
            PageRepository pageRepository,
            PageFetcher pageFetcher,
            LemmaExtractorServiceImpl morphologyService
    ) {
        this.site = site;
        this.entryPointPath = URLUtils.extractEntryPointPath(site.getUrl());
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.pageFetcher = pageFetcher;
        this.lemmaExtractorService = morphologyService;
    }

    @Override
    public IndexingStatus compute() {
        if (URLUtils.isNonHttpLink(url)) return IndexingStatus.FAILED;

        Page existingPage = pageRepository.findBySiteIdAndPathContaining(
                site.getId(),
                URLUtils.toRelativePath(url)
        );

        if (existingPage != null)
            pageRepository.delete(existingPage);

        try {
            processIndexPage();
        } catch (IndexingServiceException e) {
            siteRepository.updateLastErrorAndStatusTime(site.getId(), e.getMessage());
            return IndexingStatus.FAILED;
        }

        return IndexingStatus.INDEXED;
    }

    private void processIndexPage() throws IndexingServiceException {
        Page page = pageFetcher.fetchPage(url);

        page.setPath(page.getPath().replaceAll("^" + entryPointPath, "/"));

        page.setSite(site);

        String content = page.getContent();
        if (content == null) return;

        Map<String, Integer> lemmasFrequencyMap =
                lemmaExtractorService.buildLemmaFrequencyMap(
                        HtmlUtils.stripHtmlTags(page.getContent())
                );
        LemmaIndexMapper.mapToEntities(page, site, lemmasFrequencyMap);

        savePageWithRetry(page);
        siteRepository.updateStatusTime(site.getId());
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
