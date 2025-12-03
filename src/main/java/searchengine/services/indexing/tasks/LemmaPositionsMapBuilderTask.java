package searchengine.services.indexing.tasks;

import searchengine.services.morphology.MorphologyAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LemmaPositionsMapBuilderTask extends RecursiveTask<Map<String, List<Integer>>> {
    private final String text;
    private final int indexOffset;
    private final List<String> lemmaList;
    private final ConcurrentHashMap<String, List<Integer>> lemmasPositionsMap;
    private final MorphologyAnalyzer morphology;

    public LemmaPositionsMapBuilderTask(String text, MorphologyAnalyzer morphology, List<String> lemmaList) {
        this(
                text,
                0,
                new ConcurrentHashMap<>(),
                morphology,
                lemmaList
        );
    }

    public LemmaPositionsMapBuilderTask(String text, MorphologyAnalyzer morphology) {
        this(
                text,
                0,
                new ConcurrentHashMap<>(),
                morphology,
                List.of()
        );
    }

    private LemmaPositionsMapBuilderTask(
            String text,
            int indexOffset,
            ConcurrentHashMap<String, List<Integer>> lemmasPositionsMap,
            MorphologyAnalyzer morphology,
            List<String> lemmaList
            ) {
        this.text = text.toLowerCase(Locale.ROOT);
        this.indexOffset = indexOffset;
        this.lemmasPositionsMap = lemmasPositionsMap;
        this.morphology = morphology;
        this.lemmaList = lemmaList;
    }

    private LemmaPositionsMapBuilderTask createSubTask(String text, int indexOffset) {
        return new LemmaPositionsMapBuilderTask(
                text,
                indexOffset,
                lemmasPositionsMap,
                morphology,
                lemmaList
        );
    }
    @Override
    protected Map<String, List<Integer>> compute() {
        int charsPerTask = 10_000;

        if(text.length() < charsPerTask) {
            Pattern pattern = Pattern.compile("([a-zA-Zа-яА-Я-]+)");
            Matcher matcher = pattern.matcher(text);

            matcher.results().forEach(match -> {
                String lemma = morphology.getWordNormalForm(matcher.group(1));
                if (lemma == null) return;
                int index = match.start(1) + indexOffset;

                if (!lemmaList.isEmpty() && !lemmaList.contains(lemma)) return;

                lemmasPositionsMap.compute(lemma, (key, value) -> {
                    if (value == null) value = new ArrayList<>();
                    value.add(index);
                    return value;
                });

            });
        } else {
            divideProcessingWordsList(text);
        }

        return lemmasPositionsMap;
    }

    private void divideProcessingWordsList(String text) {
        int splitIndex = text.indexOf(" ", text.length() / 2);
        String leftBranch = text.substring(0, splitIndex);
        String rightBranch = text.substring(splitIndex);
        LemmaPositionsMapBuilderTask leftSubTask = createSubTask(leftBranch, indexOffset);
        LemmaPositionsMapBuilderTask rightSubTask = createSubTask(rightBranch,indexOffset + splitIndex);
        leftSubTask.fork();
        rightSubTask.fork();
        leftSubTask.join();
        rightSubTask.join();
    }
}
