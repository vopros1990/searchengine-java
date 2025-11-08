package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query(value = "SELECT content FROM page WHERE site_id=:siteId AND path=:path;", nativeQuery = true)
    Optional<String> getPageContentBySiteIdAndPath(int siteId, String path);

    @Query(value = "SELECT * FROM page WHERE site_id=:siteId AND path like :path;", nativeQuery = true)
    Page findBySiteIdAndPathContaining(int siteId, String path);

    @Query(value = "select count(id) from page where site_id=?;", nativeQuery = true)
    int countSitePages(int siteId);

    @Query(value = "select id from page where site_id=?;", nativeQuery = true)
    List<Integer> getSitePagesIds(int siteId);

    @Modifying
    @Query(value = "delete from page where site_id=?;", nativeQuery = true)
    void deleteBySiteId(int siteId);
}
