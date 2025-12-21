package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.repositories.PageRepository;
import searchengine.services.searching.SearchQueryExecutor;

import java.util.List;

@Service
@ConditionalOnProperty(
        prefix = "searching",
        name = "mode",
        havingValue = "sql"
)
@RequiredArgsConstructor
public class SqlSearchQueryExecutor implements SearchQueryExecutor {
    private final PageRepository pageRepository;

    @Override
    public int getSearchResults(SearchRequestDto request, List<String> searchTerms, List<SearchResultDto> results) {
        boolean searchInAllSites = request.getSite() == null;

        int resultsCount = searchInAllSites ?
                pageRepository.searchPagesTotalCount(searchTerms, searchTerms.size()) :
                pageRepository.searchPagesTotalCount(searchTerms, searchTerms.size(), request.getSite());

        int offset = request.getOffset();
        int limit = request.getLimit() == 0 ? 10 : request.getLimit();

        List<SearchResultDto> foundPages = searchInAllSites ?
                pageRepository.searchPages(searchTerms, searchTerms.size(), offset, limit) :
                pageRepository.searchPages(searchTerms, searchTerms.size(), offset, limit, request.getSite());

        results.addAll(foundPages);

        return resultsCount;
    }
}
