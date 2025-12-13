package searchengine.services.data;

import jakarta.transaction.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

public interface DatabaseCleanupService {
    void clearSiteIndexingData(Site site);
    void clearPageIndexingData(Site site, String pageUrl);
}
