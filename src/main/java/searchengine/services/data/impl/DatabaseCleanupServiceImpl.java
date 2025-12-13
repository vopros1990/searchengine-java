package searchengine.services.data.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.common.text.URLUtils;
import searchengine.model.Page;
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
    public void clearSiteIndexingData(Site site) {
        int siteId = site.getId();
        indexRepository.deleteBySiteId(siteId);
        lemmaRepository.deleteBySiteId(siteId);
        pageRepository.deleteBySiteId(siteId);
        siteRepository.deleteById(siteId);
    }

    @Override
    public void clearPageIndexingData(Site site, String pageUrl) {
        Page existingPage = pageRepository.findBySiteIdAndPathContaining(
                site.getId(),
                URLUtils.toRelativePath(pageUrl)
        );

        if (existingPage != null) {
            int id = existingPage.getId();
            indexRepository.deleteByPageId(id);
            lemmaRepository.deleteByPageId(id);
            pageRepository.deleteById(id);
        }
    }

}
