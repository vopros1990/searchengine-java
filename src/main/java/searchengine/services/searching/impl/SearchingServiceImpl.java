package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.common.text.HtmlUtils;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.exceptions.BadRequestSearchingException;
import searchengine.exceptions.NotIndexedSiteSearchingException;
import searchengine.exceptions.SearchingException;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;
import searchengine.repositories.*;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.services.searching.SearchQueryExecutor;
import searchengine.services.searching.SearchingService;
import searchengine.services.searching.Snippet;
import searchengine.services.searching.SnippetService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchingServiceImpl implements SearchingService {
    private final SiteRepository siteRepository;
    private final LemmaExtractorServiceImpl lemmaExtractor;
    private final SearchQueryExecutor searchQueryExecutor;
    private final SnippetService snippetBuilder;
    private final List<Site> indexingSites;

    @Override
    public int search(SearchRequestDto request, List<SearchResultDto> results) throws SearchingException {
        long start = System.currentTimeMillis();
        handleExceptions(request);

        String query = request.getQuery();

        List<String> searchTerms = new ArrayList<>(lemmaExtractor
                .buildLemmaFrequencyMap(query).keySet());

        List<SearchResultDto> foundPages = new ArrayList<>();
        int totalPagesCount = searchQueryExecutor.getSearchResults(request, searchTerms, foundPages);

        if (foundPages.isEmpty())
            return 0;

        for (SearchResultDto result : foundPages) {
            String content = result.getSnippet();
            Snippet snippet = snippetBuilder.buildSnippet(content, searchTerms);
            result.setTitle(HtmlUtils.getTagContent("title", content));
            result.setSnippet(snippet.getContent());
            result.setUri(result.getUri());
            result.setRelevance(result.getRelevance() * snippet.getSearchTermsRelevance());
        }

        foundPages.sort(Comparator.comparingDouble(SearchResultDto::getRelevance).reversed());
        results.addAll(foundPages);

        log.info("Query performed in {} ms", System.currentTimeMillis() - start);
        return totalPagesCount;
    }

    private void handleExceptions(SearchRequestDto request) throws SearchingException {
        String query = request.getQuery();
        String siteUrl = request.getSite();

        if (query == null || query.isEmpty())
            throw new BadRequestSearchingException("Пустой поисковый запрос");

        if (siteUrl == null && !siteRepository.existsByStatus(IndexingStatus.INDEXED))
            throw new NotIndexedSiteSearchingException("Сайты не проиндексированы");

        if (siteUrl != null && indexingSites.stream().noneMatch(indexingSite -> indexingSite.getUrl().equals(siteUrl)))
            throw new BadRequestSearchingException("Сайт отсутствует в списке сайтов, разрешенных к индексации");

        if (siteUrl != null && !siteRepository.existsByStatusAndUrl(IndexingStatus.INDEXED, siteUrl))
            throw new NotIndexedSiteSearchingException("Сайт не проиндексирован");
    }
}
