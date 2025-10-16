package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.IndexingStatus;
import searchengine.config.SitesList;
import searchengine.exceptions.IndexingStartException;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;
import searchengine.services.CrawlerService;
import searchengine.services.IndexingService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final SitesList sitesConfigList;
    private final CrawlerService crawlerService;

    @Override
    public void startIndexing() throws IndexingStartException {
        if (!siteRepository.findByStatus(IndexingStatus.INDEXING).isEmpty())
            throw new IndexingStartException("Индексация уже запущена");

        siteRepository.deleteAll();

        List<Site> indexingSites = sitesConfigList.getSites().stream().map(
                siteConfig -> {
                    Site siteEntity = new Site();
                    siteEntity.setUrl(siteConfig.getUrl());
                    siteEntity.setName(siteConfig.getName());
                    siteEntity.setStatus(IndexingStatus.INDEXING);
                    siteEntity.setStatusTime(LocalDateTime.now());
                    return siteEntity;
                }).toList();

        indexingSites = siteRepository.saveAll(indexingSites);
        indexingSites.forEach(crawlerService::crawl);
    }

    @Override
    public void stopIndexing() throws IndexingStartException {
        crawlerService.stopCrawling();
    }
}
