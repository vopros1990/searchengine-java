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
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.services.searching.Snippet;
import searchengine.services.searching.SnippetService;
import searchengine.services.searching.SearchingService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchingServiceImpl implements SearchingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaExtractorServiceImpl lemmaExtractor;
    private final SnippetService snippetBuilder;
    private final List<Site> indexingSites;

    @Override
    public int search(SearchRequestDto request, List<SearchResultDto> results) throws SearchingException {
        handleExceptions(request);

        String query = request.getQuery();

        List<String> searchTerms = new ArrayList<>(lemmaExtractor
                .buildLemmaFrequencyMap(query).keySet());

        List<SearchResultDto> foundPages = new ArrayList<>();
        int resultsCount = getSearchResults(request, searchTerms, foundPages);
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

        return resultsCount;
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

    private int getSearchResults(SearchRequestDto request, List<String> searchTerms, List<SearchResultDto> results) {
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
