package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.dto.statistics.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.service.IndexingService;
import searchengine.services.searching.SearchingService;
import searchengine.services.statistics.StatisticsService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchingService searchingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ApiResponse> startIndexing() {
        indexingService.startIndexing();
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        indexingService.stopIndexing();
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam(name = "url") String url) {
        indexingService.indexPage(url);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/search")
    public ApiResponse search(SearchRequestDto requestDto) {
        ApiResponse response = ApiResponse.ok();
        List<SearchResultDto> searchResults = new ArrayList<>();
        int foundPagesCount = searchingService.search(requestDto, searchResults);
        response.setData(searchResults);
        response.setCount(foundPagesCount);

        return response;
    }
}
