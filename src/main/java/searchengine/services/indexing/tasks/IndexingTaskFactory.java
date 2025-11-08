package searchengine.services.indexing.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.crawler.PageFetcher;
import searchengine.services.lemma.impl.LemmaExtractor;

@Component
@RequiredArgsConstructor
public class IndexingTaskFactory {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageFetcher pageFetcher;
    private final LemmaExtractor lemmaExtractor;

    public SiteIndexingTask buildSiteIndexingTask(Site site) {
        return new SiteIndexingTask(site, siteRepository, pageRepository, pageFetcher, lemmaExtractor);
    }

    public SinglePageIndexingTask buildSinglePageIndexingTask(Site site, String url) {
        return new SinglePageIndexingTask(site, url, siteRepository, pageRepository, pageFetcher, lemmaExtractor);
    }
}
