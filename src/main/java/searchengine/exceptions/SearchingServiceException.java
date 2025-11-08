package searchengine.exceptions;

import lombok.Getter;

@Getter
public class SearchingServiceException extends RuntimeException {
    private int statusCode;

    public SearchingServiceException(String message) {
        super(message);
    }

    public SearchingServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
