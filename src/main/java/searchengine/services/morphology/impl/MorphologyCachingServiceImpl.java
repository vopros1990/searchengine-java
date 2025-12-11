package searchengine.services.morphology.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.common.task.TaskExecutor;
import searchengine.dto.cache.LemmaCacheDto;
import searchengine.repositories.LemmaCacheRepository;
import searchengine.services.morphology.MorphologyCachingService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MorphologyCachingServiceImpl implements MorphologyCachingService {
    private final LemmaCacheRepository repository;
    private final ConcurrentHashMap<String, String> cache;

    public MorphologyCachingServiceImpl(
            LemmaCacheRepository repository,
            TaskExecutor taskExecutor
    ) {
        this.repository = repository;
        this.cache = new ConcurrentHashMap<>();
        taskExecutor.runAsync(this::loadCacheFromDb);
    }

    @Override
    public String getWordLemma(String word) {
        String result;
        if ((result = cache.get(word)) != null) return result;

        return repository.findLemmaByWord(word);
    }

    @Override
    public void saveWordLemma(String word, String lemma) {
        try {
            repository.save(word, lemma);
            cache.put(word, lemma);
        } catch (Exception e) {
            log.debug(e.getMessage());
            log.debug(word);
        }
    }

    private void loadCacheFromDb() {
        int limit = 100;
        List<LemmaCacheDto> cacheBatch;

        for (int offset = 0; ; offset += limit) {
            if ((cacheBatch = repository.getWordLemmaMap(limit, offset)).isEmpty())
                return;

            cache.putAll(mapCacheDtoListToMap(cacheBatch));
        }
    }

    private Map<String, String> mapCacheDtoListToMap(List<LemmaCacheDto> dtoList) {
        return dtoList.stream().collect(Collectors.toMap(LemmaCacheDto::getWord, LemmaCacheDto::getLemma));
    }
}
