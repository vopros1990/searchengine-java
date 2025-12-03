package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.dto.search.SearchResultDto;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query(value = "SELECT * FROM page WHERE site_id=:siteId AND path like :path;", nativeQuery = true)
    Page findBySiteIdAndPathContaining(int siteId, String path);

    @Query(value = """
            WITH
            	total_pages AS (
            		SELECT COUNT(1) AS count, site_id
            		FROM page
            		GROUP BY site_id
            	),
            	matched_pages AS (
            		SELECT i.page_id AS page_id, SUM(i.rank * LOG(tp.count / l.frequency)) AS `rank`
            		FROM `index` i
            		LEFT JOIN lemma l ON l.id=i.lemma_id
            		LEFT JOIN total_pages tp ON l.site_id=tp.site_id
            		WHERE l.lemma IN :searchTerms
            		GROUP BY i.page_id
            		HAVING count(l.lemma) = :searchTermsSize
            		ORDER BY i.page_id
            	),
            	page_relevance AS (
            		SELECT p.id
            		FROM page p
            	),
            	max_rank AS (
            		SELECT `rank` FROM matched_pages ORDER BY `rank` DESC LIMIT 1
            	)
            SELECT
            	s.url as site,
               	s.name as siteName,
               	p.path as uri,
               	'' as title,
               	p.content as snippet,
               	mp.rank/mr.rank AS relevance
            FROM max_rank as mr, matched_pages mp
                LEFT JOIN page p ON p.id=mp.page_id
                LEFT JOIN total_pages tp ON p.site_id=tp.site_id
                LEFT JOIN site s ON p.site_id=s.id
                ORDER BY relevance DESC
                LIMIT :limit OFFSET :offset;
            """, nativeQuery = true)
    List<SearchResultDto> searchPages(List<String> searchTerms, Integer searchTermsSize, Integer offset, Integer limit);

    @Query(value = """
            WITH
            	total_pages AS (
            		SELECT COUNT(1) AS count, p.site_id
            		FROM page p
            		LEFT JOIN site s ON p.site_id=s.id
            		WHERE s.url=:siteUrl
            		GROUP BY site_id
            	),
            	matched_pages AS (
            		SELECT i.page_id AS page_id, SUM(i.rank * LOG(tp.count / l.frequency)) AS `rank`
            		FROM `index` i
            		LEFT JOIN lemma l ON l.id=i.lemma_id
            		LEFT JOIN total_pages tp ON l.site_id=tp.site_id
            		WHERE l.lemma IN :searchTerms AND l.site_id=tp.site_id
            		GROUP BY i.page_id
            		HAVING count(l.lemma) = :searchTermsSize
            		ORDER BY i.page_id
            	),
            	page_relevance AS (
            		SELECT p.id
            		FROM page p
            	),
            	max_rank AS (
            		SELECT `rank` FROM matched_pages ORDER BY `rank` DESC LIMIT 1
            	)
            SELECT
            	s.url as site,
               	s.name as siteName,
               	p.path as uri,
               	'' as title,
               	p.content as snippet,
               	mp.rank/mr.rank AS relevance
            FROM max_rank as mr, matched_pages mp
                LEFT JOIN page p ON p.id=mp.page_id
                LEFT JOIN total_pages tp ON p.site_id=tp.site_id
                LEFT JOIN site s ON p.site_id=s.id
                ORDER BY relevance DESC
                LIMIT :limit OFFSET :offset;
            """, nativeQuery = true)
    List<SearchResultDto> searchPages(List<String> searchTerms, Integer searchTermsSize, Integer offset, Integer limit, String siteUrl);

    @Query(value = "select count(id) from page where site_id=?;", nativeQuery = true)
    int countSitePages(int siteId);

    @Query(value = "select id from page where site_id=?;", nativeQuery = true)
    List<Integer> getSitePagesIds(int siteId);

    @Modifying
    @Query(value = "delete from page where site_id=?;", nativeQuery = true)
    void deleteBySiteId(int siteId);


}
