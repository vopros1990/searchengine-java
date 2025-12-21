package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.model.Site;
import searchengine.repositories.PageRepositoryPageable;
import searchengine.repositories.SiteRepository;
import searchengine.services.searching.SearchQueryExecutor;

import java.util.List;

@Service
@ConditionalOnProperty(
        prefix = "searching",
        name = "mode",
        havingValue = "jpql",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class JpqlSearchQueryExecutor implements SearchQueryExecutor {
    private final SiteRepository siteRepository;
    private final PageRepositoryPageable pageRepository;

    @Override
    public int getSearchResults(SearchRequestDto request, List<String> searchTerms, List<SearchResultDto> results) {
        boolean searchInAllSites = request.getSite() == null;
        Site site = siteRepository.findFirstByUrlContaining(request.getSite());

        int pageNumber = request.getOffset() / request.getLimit();
        Pageable paging = PageRequest.of(pageNumber, request.getLimit());

        Page<SearchResultDto> resultsPage = searchInAllSites ?
                pageRepository.searchPagesByLemmasOrderByRelevance(searchTerms, searchTerms.size(), paging) :
                pageRepository.searchPagesByLemmasOrderByRelevance(searchTerms, searchTerms.size(), paging, site);

        results.addAll(resultsPage.getContent());

        return (int) resultsPage.getTotalElements();
    }
}
