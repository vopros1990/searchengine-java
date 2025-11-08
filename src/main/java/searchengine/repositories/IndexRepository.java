package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    @Modifying
    @Query(value = "delete from `index` where page_id=?;", nativeQuery = true)
    void deleteByPageId(int pageId);
}
