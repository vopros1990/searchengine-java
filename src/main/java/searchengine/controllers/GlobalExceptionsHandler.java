package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.statistics.ApiResponse;
import searchengine.exceptions.BadRequestSearchingException;
import searchengine.exceptions.IndexingException;
import searchengine.exceptions.NotIndexedSiteSearchingException;
import searchengine.exceptions.SearchingException;

@RestControllerAdvice
public class GlobalExceptionsHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestSearchingException.class)
    public ApiResponse handleBadRequestSearchingServiceException(SearchingException e) {
        return ApiResponse.error(e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotIndexedSiteSearchingException.class)
    public ApiResponse handleNNotIndexedSiteSearchingServiceException(SearchingException e) {
        return ApiResponse.error(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ApiResponse handleIndexingException(IndexingException e) {
        return ApiResponse.error(e.getMessage());
    }
}
