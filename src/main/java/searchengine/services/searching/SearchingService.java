package searchengine.services.searching;

import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.exceptions.SearchingServiceException;

import java.util.List;

public interface SearchingService {
    List<SearchResultDto> search(SearchRequestDto requestDto) throws SearchingServiceException;
}
