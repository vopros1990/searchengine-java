package searchengine.services.indexing.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.common.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class LemmaExtractorTask extends RecursiveTask<Map<String, Integer>> {
    private final List<String> words;
    private final ConcurrentHashMap<String, List<String>> wordsBaseFormsCache;
    private final ConcurrentHashMap<String, Integer> lemmasFrequencyMap;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ARTICLE", "CONJ"};


    public LemmaExtractorTask(List<String> words, ConcurrentHashMap<String, List<String>> wordsBaseFormsCache) {
        this(
                words,
                new ConcurrentHashMap<>(),
                wordsBaseFormsCache
        );
    }

    private LemmaExtractorTask(
            List<String> words,
            ConcurrentHashMap<String, Integer> lemmasFrequencyMap,
            ConcurrentHashMap<String, List<String>> wordsBaseFormsCache) {
        this.words = words;
        this.lemmasFrequencyMap = lemmasFrequencyMap;
        this.wordsBaseFormsCache = wordsBaseFormsCache;
    }

    private LemmaExtractorTask createSubTask(List<String> words) {
        return new LemmaExtractorTask(
                words,
                lemmasFrequencyMap,
                wordsBaseFormsCache
        );
    }

    @Override
    protected Map<String, Integer> compute() {
        int itemsPerTask = 10;

        if(words.size() < itemsPerTask) {
            words.forEach(word -> {
                if (lemmasFrequencyMap.computeIfPresent(word, (key, value) -> ++value) != null) return;
                getWordNormalForms(word).forEach(lemma -> {
                    if (lemmasFrequencyMap.computeIfPresent(lemma, (key, value) -> ++value) != null) return;
                    lemmasFrequencyMap.computeIfAbsent(lemma, (key) -> 1);
                });
            });
        } else {
            divideProcessingWordsList(words);
        }

        return lemmasFrequencyMap;
    }

    private void divideProcessingWordsList(List<String> words) {
        int splitIndex = words.size() / 2;
        List<String> leftBranch = words.subList(0, splitIndex);
        List<String> rightBranch = words.subList(splitIndex + 1, words.size() - 1);
        LemmaExtractorTask leftSubTask = createSubTask(leftBranch);
        LemmaExtractorTask rightSubTask = createSubTask(rightBranch);
        leftSubTask.fork();
        rightSubTask.fork();
        leftSubTask.join();
        rightSubTask.join();
    }

    private List<String> getWordNormalForms(String word) {
        List<String> wordNormalForms;

        if ((wordNormalForms = getCachedWordNormalForms(word)) != null) return wordNormalForms;

        try {
            LuceneMorphology morphology;

            if (TextUtils.isLatinWord(word)) morphology = new EnglishLuceneMorphology();
            else if (TextUtils.isCyrillicWord(word)) morphology = new RussianLuceneMorphology();
            else return cacheAndReturnWordNormalForms(word, List.of(word));

            if (morphology.getMorphInfo(word).stream().anyMatch(this::isParticle))
                return cacheAndReturnWordNormalForms(word, List.of());

            return cacheAndReturnWordNormalForms(word, morphology.getNormalForms(word));
        } catch (IOException e) {
            log.info(e.getMessage());
        }

        return List.of();
    }

    private List<String> getCachedWordNormalForms(String word) {
        return wordsBaseFormsCache.get(word);
    }

    private List<String> cacheAndReturnWordNormalForms(String word, List<String> wordNormalForms) {
        wordsBaseFormsCache.put(word, wordNormalForms);
        return wordNormalForms;
    }

    private boolean isParticle(String wordMorphInfo) {
        for (String particle : particlesNames) {
            if (wordMorphInfo.toUpperCase(Locale.ROOT).contains(particle)) return true;
        }

        return false;
    }
}
