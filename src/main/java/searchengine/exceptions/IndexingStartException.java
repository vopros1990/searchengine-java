package searchengine.exceptions;

public class IndexingStartException extends RuntimeException {
    public IndexingStartException(String errorMessage) {
        super(errorMessage);
    }
}
