package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Query(value = "SELECT COUNT(DISTINCT(lemma)) FROM lemma", nativeQuery = true)
    int countUniqueLemmas();

    @Query(value = "SELECT COUNT(1) FROM lemma WHERE site_id=?;", nativeQuery = true)
    int countSiteLemmas(int siteId);

    @Modifying
    @Query(value = "DELETE FROM lemma WHERE site_id=?;", nativeQuery = true)
    void deleteBySiteId(int siteId);
}
