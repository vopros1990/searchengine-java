package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.statistics.ApiResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.exceptions.IndexingException;
import searchengine.services.indexing.service.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

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


        return ResponseEntity.ok(ApiResponse.ok());
    }
}
