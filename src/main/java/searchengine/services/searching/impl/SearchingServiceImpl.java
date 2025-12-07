package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.common.text.HtmlUtils;
import searchengine.common.text.URLUtils;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.exceptions.SearchingServiceException;
import searchengine.repositories.PageRepository;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.services.searching.Snippet;
import searchengine.services.searching.SnippetService;
import searchengine.services.searching.SearchingService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchingServiceImpl implements SearchingService {
    private final PageRepository pageRepository;
    private final LemmaExtractorServiceImpl lemmaExtractor;
    private final SnippetService snippetBuilder;

    @Override
    public List<SearchResultDto> search(SearchRequestDto request) throws SearchingServiceException {
        String query = request.getQuery();

        if (query == null || query.isEmpty())
            throw new SearchingServiceException("Пустой поисковый запрос", 400);

        boolean searchInAllSites = request.getSite() == null;

        List<String> searchTerms = new ArrayList<>(lemmaExtractor
                .buildLemmaFrequencyMap(query).keySet());
        int offset = request.getOffset();
        int limit = request.getLimit() == 0 ? 10 : request.getLimit();

        List<SearchResultDto> results = searchInAllSites ?
                pageRepository.searchPages(searchTerms, searchTerms.size(), offset, limit) :
                pageRepository.searchPages(searchTerms, searchTerms.size(), offset, limit, request.getSite());

        if (results.isEmpty())
            return List.of();

        for (SearchResultDto result : results) {
            String content = result.getSnippet();
            Snippet snippet = snippetBuilder.buildSnippet(content, searchTerms);
            result.setTitle(HtmlUtils.getTagContent("title", content));
            result.setSnippet(snippet.getContent());
            result.setUri(URLUtils.removeLeadingSlash(result.getUri()));
            result.setRelevance(result.getRelevance() * snippet.getSearchTermsRelevance());
        }

        results.sort(Comparator.comparingDouble(SearchResultDto::getRelevance).reversed());

        return results;
    }
}
