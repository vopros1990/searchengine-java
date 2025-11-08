package searchengine.exceptions;

public class IndexingException extends RuntimeException {
    public IndexingException(String errorMessage) {
        super(errorMessage);
    }
}
