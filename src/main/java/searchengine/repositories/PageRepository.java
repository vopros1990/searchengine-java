package searchengine.repositories;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(nativeQuery = true, value = "SELECT id FROM page WHERE site_id=:siteId AND path=:path")
    Optional<Integer> existsBySiteIdAndPath(int siteId, String path);

    @Query(nativeQuery = true, value = "SELECT content FROM page WHERE site_id=:siteId AND path=:path")
    Optional<String> getPageContentBySiteIdAndPath(int siteId, String path);

    @Modifying
    @Transactional
    @Query(value = "insert into page (path, site_id, code, content) " +
            "values (:#{#page.path}, :#{#page.siteId}, :#{#page.code}, :#{#page.content}) " +
            "on duplicate key update code = :#{#page.code};", nativeQuery = true)
    void saveOnDuplicateKeyUpdateCode(@Param("page") Page page);
}
