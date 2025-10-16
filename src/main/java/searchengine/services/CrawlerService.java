package searchengine.services;

import searchengine.model.Site;

public interface CrawlerService {
    void crawl(Site site);
    void stopCrawling();
}
