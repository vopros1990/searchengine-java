package searchengine.services.morphology;

public interface MorphologyCachingService {
    String getWordLemma(String word);
    void saveWordLemma(String word, String lemma);
}
