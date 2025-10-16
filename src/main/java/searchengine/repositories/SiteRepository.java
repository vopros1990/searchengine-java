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
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    List<Site> findByStatus(IndexingStatus status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now() WHERE id = :siteId;", nativeQuery = true)
    void updateStatusTime(@Param("siteId") int id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE site SET status_time = now(), last_error = :lastError WHERE id = :siteId;", nativeQuery = true)
    void updateLastErrorAndStatusTime(@Param("siteId") int id, @Param("lastError") String lastError);
}
