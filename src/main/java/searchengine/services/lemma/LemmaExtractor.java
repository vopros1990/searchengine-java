package searchengine.services.lemma;

import java.util.Map;

public interface LemmaExtractor {
    Map<String, Integer> buildLemmasFrequencyMap(String content);
}
