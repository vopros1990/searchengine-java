package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticsResponse extends ApiResponse {
    private StatisticsData statistics;
}
