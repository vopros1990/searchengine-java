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
    List<Site> findByStatus(IndexingStatus status);

    boolean existsByStatus(IndexingStatus status);

    Site findByUrlContaining(String url);

    @Query(value = "SELECT * FROM site WHERE url IN ?;", nativeQuery = true)
    List<Site> findByUrls(List<String> urls);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now() WHERE id = :siteId;", nativeQuery = true)
    void updateStatusTime(@Param("siteId") int id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now(), last_error = :lastError WHERE id = :siteId;", nativeQuery = true)
    void updateLastErrorAndStatusTime(@Param("siteId") int id, @Param("lastError") String lastError);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now(), status = :status WHERE id = :siteId;", nativeQuery = true)
    void updateStatusAndStatusTime(@Param("siteId") int id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now(), last_error = :lastError, status = :status WHERE id = :siteId;", nativeQuery = true)
    void updateLastErrorAndStatusAndStatusTime(@Param("siteId") int id, @Param("lastError") String lastError, @Param("status") String status);

    @Modifying
    @Query(value = "delete from site where id=?;", nativeQuery = true)
    void deleteBySiteId(int id);
}
