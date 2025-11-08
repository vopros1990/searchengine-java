package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    void findBySiteId(int siteId);

    @Query(value = "select count(id) from lemma where site_id=?;", nativeQuery = true)
    int countSiteLemmas(int siteId);

    @Modifying
    @Query(value = "delete from lemma where site_id=?;", nativeQuery = true)
    void deleteBySiteId(int siteId);
}
