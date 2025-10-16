package searchengine.services;

import searchengine.exceptions.IndexingStartException;

public interface IndexingService {
    void startIndexing() throws IndexingStartException;
    void stopIndexing() throws IndexingStartException;
}
