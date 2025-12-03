package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResultDto;
import searchengine.dto.statistics.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exceptions.IndexingException;
import searchengine.exceptions.SearchingServiceException;
import searchengine.services.indexing.service.IndexingService;
import searchengine.services.searching.SearchingService;
import searchengine.services.statistics.StatisticsService;

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
        try {
            indexingService.startIndexing();
        } catch (IndexingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.error(e.getMessage())
                    );
        }

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiResponse> stopIndexing() {
        try {
            indexingService.stopIndexing();
        } catch (IndexingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.error(e.getMessage())
                    );
        }

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiResponse> indexPage(@RequestParam(name = "url") String url) {
        try {
            indexingService.indexPage(url);
        } catch (IndexingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            ApiResponse.error(e.getMessage())
                    );
        }

        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(SearchRequestDto requestDto) {
        try {
            ApiResponse response = ApiResponse.ok();
            List<SearchResultDto> searchResults = searchingService.search(requestDto);
            response.setData(searchResults);
            response.setCount(searchResults.size());

            return ResponseEntity.ok(response);
        } catch (SearchingServiceException e) {
            return ResponseEntity.status(
                    e.getStatusCode() == null ? HttpStatus.CONFLICT : HttpStatus.valueOf(e.getStatusCode())
            ).body(
                    ApiResponse.error(e.getMessage())
            );
        }
    }
}
