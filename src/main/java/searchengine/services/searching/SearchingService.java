package searchengine.services.searching;

import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.exceptions.SearchingException;

import java.util.List;

public interface SearchingService {
    int search(SearchRequestDto requestDto, List<SearchResultDto> results) throws SearchingException;
}
