package searchengine.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LemmaCacheDto {
    private String word;
    private String lemma;
}
