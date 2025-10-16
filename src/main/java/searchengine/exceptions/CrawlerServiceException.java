package searchengine.exceptions;

import lombok.Getter;

@Getter
public class CrawlerServiceException extends RuntimeException {
    private final String url;

    public CrawlerServiceException(String url, String message) {
        super(message);
        this.url = url;
    }
}
