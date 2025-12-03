package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchResultDto implements Comparable<SearchResultDto> {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private Double relevance;

    @Override
    public int compareTo(SearchResultDto o) {
        return this.getRelevance().compareTo(o.getRelevance());
    }
}
