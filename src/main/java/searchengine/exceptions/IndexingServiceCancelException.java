package searchengine.exceptions;

public class IndexingServiceCancelException extends RuntimeException {
    public IndexingServiceCancelException(String message) {
        super(message);
    }
}
