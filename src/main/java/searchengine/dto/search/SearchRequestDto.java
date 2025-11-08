package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequestDto {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
