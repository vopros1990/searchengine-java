package searchengine.services.indexing.tasks;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import searchengine.common.task.TaskSleepBlocker;
import searchengine.exceptions.IndexingServiceException;
import searchengine.model.*;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.crawler.PageFetcher;
import searchengine.services.lemma.impl.LemmaExtractor;
import searchengine.common.text.URLUtils;

import java.util.concurrent.RecursiveTask;

@Slf4j
public class SinglePageIndexingTask extends RecursiveTask<IndexingStatus> {
    private final Site site;
    private String url;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageFetcher pageFetcher;
    private final LemmaExtractor lemmaExtractor;

    private static final int SAVE_RETRY_MAX_ATTEMPTS = 5;
    private static final int SAVE_RETRY_DELAY_MILLIS = 50;

    public SinglePageIndexingTask(
            Site site,
            String url,
            SiteRepository siteRepository,
            PageRepository pageRepository,
            PageFetcher pageFetcher,
            LemmaExtractor lemmaExtractor
    ) {
        this.site = site;
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.pageFetcher = pageFetcher;
        this.lemmaExtractor = lemmaExtractor;
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
        page.setSite(site);

        String content = page.getContent();
        if (content == null) return;

        lemmaExtractor.buildLemmasFrequencyMap(content)
                .forEach((word, frequency) -> {
                    Lemma lemma = new Lemma();
                    lemma.setSite(site);
                    lemma.setLemma(word);
                    lemma.setFrequency(1);

                    Index index = new Index();
                    index.setPage(page);
                    index.setRank((float) frequency);

                    lemma.addIndex(index);
                    page.addIndex(index);
                });

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
