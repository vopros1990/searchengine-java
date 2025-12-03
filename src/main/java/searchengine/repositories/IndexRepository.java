package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Query(value = "delete from `index` where page_id=?;", nativeQuery = true)
    void deleteByPageId(int pageId);

    @Query(value = """
            WITH
            	matched_pages AS (
            		SELECT i.page_id FROM `index` i
            		LEFT JOIN lemma l ON l.id=i.lemma_id
            		WHERE l.lemma IN :targetLemmas
            		GROUP BY i.page_id
            		HAVING count(l.lemma) = :numberOfLemmas
            		ORDER BY i.page_id
            	),
            	total_pages AS (
            		SELECT COUNT(1) AS count, site_id
            		FROM page
            		GROUP BY site_id
            	)
            SELECT
            	i.id, i.page_id, i.lemma_id,
            	LOG(total_pages.count / l.frequency) AS `rank`
                FROM `index` i
                LEFT JOIN lemma l on i.lemma_id = l.id
                LEFT JOIN total_pages ON l.site_id=total_pages.site_id
                WHERE i.page_id IN (SELECT * from matched_pages)
               AND l.lemma IN :targetLemmas;
            """, nativeQuery = true)
    List<Index> findIndexesContainingAllLemmas(List<String> targetLemmas, Integer numberOfLemmas);

    @Query(value = """
            WITH
            	min_frequency_lemma AS (
            		SELECT l.lemma, l.site_id
            		FROM lemma l LEFT JOIN site s ON l.site_id=s.id
            		WHERE lemma IN :targetLemmas AND s.url=:siteUrl
            		ORDER BY frequency ASC
            		LIMIT 1
            	),
            	total_pages AS (
            		SELECT COUNT(1) AS count
            		FROM page
            		WHERE site_id=(SELECT site_id FROM min_frequency_lemma)
            	)
            SELECT
            	i.id, i.page_id, i.lemma_id,
            	LOG(total_pages.count / l.frequency) AS `rank`
                FROM min_frequency_lemma, total_pages, `index` i LEFT JOIN lemma l on i.lemma_id = l.id
                WHERE l.lemma=min_frequency_lemma.lemma AND l.site_id=min_frequency_lemma.site_id
                ORDER BY l.frequency ASC;
            """, nativeQuery = true)
    List<Index> findMinLemmaFrequencyIndexes(List<String> targetLemmas, String siteUrl);

    @Query(value = """
            WITH
            	min_frequency_lemma AS (
            		SELECT SUM(frequency) AS sum_frequency, lemma
            		FROM lemma
            		WHERE lemma IN :targetLemmas
            		GROUP BY lemma
            		ORDER BY sum_frequency ASC LIMIT 1
            	),
            	total_pages AS (
            		SELECT COUNT(1) AS count, site_id
            		FROM page
            		GROUP BY site_id
            	)
            SELECT
            	i.id, i.page_id, i.lemma_id,
            	LOG(total_pages.count / l.frequency) AS `rank`
                FROM min_frequency_lemma, `index` i
                LEFT JOIN lemma l on i.lemma_id = l.id
                LEFT JOIN total_pages ON l.site_id=total_pages.site_id
                WHERE l.lemma=min_frequency_lemma.lemma
                ORDER BY l.frequency ASC;
            """, nativeQuery = true)
    List<Index> findMinLemmaFrequencyIndexes(List<String> targetLemmas);


}
