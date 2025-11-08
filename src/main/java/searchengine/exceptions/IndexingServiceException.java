package searchengine.exceptions;

import lombok.Getter;

@Getter
public class IndexingServiceException extends RuntimeException {
    private final String url;

    public IndexingServiceException(String url, String message) {
        super(message);
        this.url = url;
    }
}
