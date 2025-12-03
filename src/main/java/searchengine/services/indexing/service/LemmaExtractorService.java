package searchengine.services.indexing.service;

import java.util.List;
import java.util.Map;

public interface LemmaExtractorService {
    Map<String, Integer> buildLemmaFrequencyMap(String text);
    Map<String, List<Integer>> buildLemmaPositionsMap(String text, List<String> lemmaList);
}
