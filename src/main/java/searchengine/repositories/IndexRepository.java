package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM index WHERE page_id=?;", nativeQuery = true)
    void deleteByPageId(int pageId);

    @Modifying
    @Transactional
    @Query(value = """
            WITH page_ids AS (
            SELECT i.page_id FROM index i LEFT JOIN page p on i.page_id=p.id WHERE p.site_id=:siteId
            ) DELETE FROM index WHERE page_id IN (SELECT * FROM page_ids);
            """, nativeQuery = true)
    void deleteBySiteId(int siteId);
}
