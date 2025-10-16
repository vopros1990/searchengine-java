package searchengine.exceptions;

public class CrawlerServiceCancelException extends RuntimeException {
    public CrawlerServiceCancelException(String message) {
        super(message);
    }
}
