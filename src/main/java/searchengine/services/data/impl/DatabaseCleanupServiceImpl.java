package searchengine.services.data.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.data.DatabaseCleanupService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseCleanupServiceImpl implements DatabaseCleanupService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    @Transactional
    public void clearIndexingData(Site site) {
        int siteId = site.getId();
        List<Integer> pageIds = pageRepository.getSitePagesIds(siteId);
        pageIds.forEach(indexRepository::deleteByPageId);
        lemmaRepository.deleteBySiteId(siteId);
        pageRepository.deleteBySiteId(siteId);
        siteRepository.deleteById(siteId);
    }
}
