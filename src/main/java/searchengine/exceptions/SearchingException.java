package searchengine.exceptions;

import lombok.Getter;

@Getter
public class SearchingException extends RuntimeException {
    public SearchingException(String message) {
        super(message);
    }
}
