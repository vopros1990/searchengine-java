package searchengine.services.data;

import searchengine.model.Site;

public interface DatabaseCleanupService {
    void clearIndexingData(Site site);
}
