package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.exceptions.SearchingServiceException;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.impl.LemmaExtractor;
import searchengine.services.searching.SearchingService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaExtractor lemmaExtractor;

    @Override
    public List<SearchResultDto> search(SearchRequestDto requestDto) throws SearchingServiceException {
        return List.of();
    }
}
