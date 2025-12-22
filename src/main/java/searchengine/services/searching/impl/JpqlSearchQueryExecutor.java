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
import searchengine.repositories.PageRepository;
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
    private final PageRepositoryPageable pageRepositoryPageable;
    private final PageRepository pageRepository;

    @Override
    public int getSearchResults(SearchRequestDto request, List<String> searchTerms, List<SearchResultDto> results) {
        boolean searchInAllSites = request.getSite() == null;
        Site site = siteRepository.findFirstByUrlContaining(request.getSite());

        int pageNumber = request.getOffset() / request.getLimit();
        Pageable paging = PageRequest.of(pageNumber, request.getLimit());

        Page<SearchResultDto> resultsPage = searchInAllSites ?
                pageRepositoryPageable.searchPagesByLemmasOrderByRelevance(searchTerms, searchTerms.size(), paging) :
                pageRepositoryPageable.searchPagesByLemmasOrderByRelevance(searchTerms, searchTerms.size(), site, paging);

        int foundPages = (int) resultsPage.getTotalElements();

        if (foundPages == 0)
            return 0;

        double maxRelevance = searchInAllSites ?
                pageRepository.getMaxRelevance(searchTerms, searchTerms.size()) :
                pageRepository.getMaxRelevance(searchTerms, searchTerms.size(), site);

        results.addAll(
                mapAbsoluteToRelativeRelevance(resultsPage.getContent(), maxRelevance)
        );

        return foundPages;
    }

    private List<SearchResultDto> mapAbsoluteToRelativeRelevance(List<SearchResultDto> searchResults, double maxRelevance) {
        searchResults.forEach(result -> result.setRelevance(
                result.getRelevance() / maxRelevance
        ));

        return searchResults;
    }
}
