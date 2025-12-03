package searchengine.services.indexing.tasks;

import searchengine.services.morphology.MorphologyAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

public class LemmaFrequencyMapBuilderTask extends RecursiveTask<Map<String, Integer>> {
    private final List<String> words;
    private final ConcurrentHashMap<String, Integer> lemmasFrequencyMap;
    private final MorphologyAnalyzer morphology;

    public LemmaFrequencyMapBuilderTask(List<String> words, MorphologyAnalyzer morphology) {
        this(
                words,
                new ConcurrentHashMap<>(),
                morphology
        );
    }

    private LemmaFrequencyMapBuilderTask(
            List<String> words,
            ConcurrentHashMap<String, Integer> lemmasFrequencyMap,
            MorphologyAnalyzer morphology) {
        this.words = words;
        this.lemmasFrequencyMap = lemmasFrequencyMap;
        this.morphology = morphology;
    }

    private LemmaFrequencyMapBuilderTask createSubTask(List<String> words) {
        return new LemmaFrequencyMapBuilderTask(
                words,
                lemmasFrequencyMap,
                morphology
        );
    }

    @Override
    protected Map<String, Integer> compute() {
        int itemsPerTask = 10;

        if(words.size() < itemsPerTask) {
            words.forEach(word -> {
                if (lemmasFrequencyMap.computeIfPresent(word, (key, value) -> ++value) != null) return;
                String lemma;
                if ((lemma = morphology.getWordNormalForm(word)) == null) return;
                lemmasFrequencyMap.compute(lemma, (key, value) -> value == null ? 1 : ++value);
            });
        } else {
            divideProcessingWordsList(words);
        }

        return lemmasFrequencyMap;
    }

    private void divideProcessingWordsList(List<String> words) {
        int splitIndex = words.size() / 2;
        List<String> leftBranch = words.subList(0, splitIndex);
        List<String> rightBranch = words.subList(splitIndex, words.size());
        LemmaFrequencyMapBuilderTask leftSubTask = createSubTask(leftBranch);
        LemmaFrequencyMapBuilderTask rightSubTask = createSubTask(rightBranch);
        leftSubTask.fork();
        rightSubTask.fork();
        leftSubTask.join();
        rightSubTask.join();
    }
}
