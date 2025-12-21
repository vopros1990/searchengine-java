package searchengine.services.searching;

import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;

import java.util.List;

public interface SearchQueryExecutor {
    int getSearchResults(
            SearchRequestDto request,
            List<String> searchTerms,
            List<SearchResultDto> results
    );
}
