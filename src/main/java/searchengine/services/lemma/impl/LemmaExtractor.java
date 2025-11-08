package searchengine.services.lemma.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.services.indexing.tasks.LemmaExtractorTask;
import searchengine.common.task.TaskExecutor;
import searchengine.common.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaExtractor implements searchengine.services.lemma.LemmaExtractor {
    private final TaskExecutor taskExecutor;
    private final ConcurrentHashMap<String, List<String>> wordsBaseFormsCache = new ConcurrentHashMap<>();

    public Map<String, Integer> buildLemmasFrequencyMap(String content) {
        List<String> words = TextUtils.extractWordsFromHtml(content);
        return taskExecutor.invoke(new LemmaExtractorTask(words, wordsBaseFormsCache));
    }
}
