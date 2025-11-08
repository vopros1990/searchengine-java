package searchengine.services.data;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseCleanupService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;

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
