package searchengine.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import searchengine.model.IndexingStatus;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM site where id=:id", nativeQuery = true)
    void deleteById(int id);

    boolean existsByStatus(IndexingStatus status);

    boolean existsByStatusAndUrl(IndexingStatus status, String url);

    Site findFirstByUrlContaining(String url);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = CURRENT_TIMESTAMP WHERE id = :siteId;", nativeQuery = true)
    void updateStatusTime(@Param("siteId") int id);
}
