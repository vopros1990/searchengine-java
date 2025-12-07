package searchengine.services.indexing.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.common.text.TextUtils;
import searchengine.services.indexing.service.LemmaExtractorService;
import searchengine.services.indexing.tasks.LemmaFrequencyMapBuilderTask;
import searchengine.common.task.TaskExecutor;
import searchengine.services.indexing.tasks.LemmaPositionsMapBuilderTask;
import searchengine.services.morphology.MorphologyAnalyzer;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LemmaExtractorServiceImpl implements LemmaExtractorService {
    private final TaskExecutor taskExecutor;
    private final MorphologyAnalyzer morphology;

    public Map<String, Integer> buildLemmaFrequencyMap(String text) {
        List<String> words = TextUtils.extractWords(text);
        return taskExecutor.invoke(new LemmaFrequencyMapBuilderTask(words, morphology));
    }

    public Map<String, List<Integer>> buildLemmaPositionsMap(String text, List<String> lemmaList) {
        return taskExecutor.invoke(new LemmaPositionsMapBuilderTask(text, morphology, lemmaList));
    }
}
