package searchengine.services.statistics.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.statistics.StatisticsService;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @NonNull
    private List<Site> indexingSites;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(indexingSites.size());
        total.setLemmas(lemmaRepository.countUniqueLemmas());
        total.setIndexing(false);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        indexingSites.forEach(site -> {
            Site indexingSite = siteRepository.findFirstByUrlContaining(site.getUrl());
            if (indexingSite == null)
                indexingSite = site;

            DetailedStatisticsItem item = new DetailedStatisticsItem();

            item.setName(indexingSite.getName());
            item.setUrl(indexingSite.getUrl());

            Integer siteId = indexingSite.getId();
            int pages = 0, lemmas = 0;
            String lastError;

            if (siteId != null) {
                pages = pageRepository.countSitePages(siteId);
                lemmas = lemmaRepository.countSiteLemmas(siteId);
                lastError = indexingSite.getLastError() == null ? "" : indexingSite.getLastError();
            } else {
                indexingSite.setStatus(IndexingStatus.FAILED);
                indexingSite.setStatusTime(Instant.now());
                lastError = "Индексация сайта не запущена";
            }

            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(indexingSite.getStatus().toString());
            item.setError(lastError);
            item.setStatusTime(indexingSite.getStatusTime().toEpochMilli());
            total.setPages(total.getPages() + pages);
            detailed.add(item);
        });

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
