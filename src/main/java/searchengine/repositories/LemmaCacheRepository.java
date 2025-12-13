package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.dto.cache.LemmaCacheDto;
import searchengine.model.LemmaCache;

import java.util.List;

@Repository
public interface LemmaCacheRepository extends JpaRepository<LemmaCache, Integer> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO lemma_cache (word, lemma) VALUES (:word, :lemma) ON CONFLICT (word) DO NOTHING;",
            nativeQuery = true)
    void save(String word, String lemma);

    @Query(value = "SELECT lemma FROM LemmaCache WHERE word=:word")
    String findLemmaByWord(String word);

    @Query(value = "SELECT word, lemma from lemma_cache LIMIT :limit OFFSET :offset;", nativeQuery = true)
    List<LemmaCacheDto> getWordLemmaMap(Integer limit, Integer offset);
}
