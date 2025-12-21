package searchengine.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Page;
import searchengine.dto.search.SearchResultDto;
import searchengine.model.Site;

import java.util.List;

public interface PageRepositoryPageable extends PagingAndSortingRepository<searchengine.model.Page, Integer> {

    @Query("""
            SELECT i.page.site.url as site,
            i.page.site.name as siteName,
            i.page.path as uri,
            '' as title,
            i.page.content as snippet,
            SUM(i.rank * LOG( (SELECT COUNT(p) FROM Page p) * 1.0 / i.lemma.frequency) ) as relevance
            FROM Index i
            WHERE i.lemma.lemma IN :lemmas
            GROUP BY i.page, i.page.site
            HAVING count(i.lemma.lemma) = :lemmasCount
            ORDER BY SUM(i.rank * LOG( (SELECT COUNT(p) FROM Page p) * 1.0 / i.lemma.frequency) ) DESC
           """)
    Page<SearchResultDto> searchPagesByLemmasOrderByRelevance(List<String> lemmas, int lemmasCount, Pageable paging);

    @Query("""
            SELECT i.page.site.url as site,
            i.page.site.name as siteName,
            i.page.path as uri,
            '' as title,
            i.page.content as snippet,
            SUM(i.rank * LOG( (SELECT COUNT(p) FROM Page p) * 1.0 / i.lemma.frequency) ) as relevance
            FROM Index i
            WHERE i.lemma.lemma IN :lemmas AND i.lemma.site = :site
            GROUP BY i.page, i.page.site
            HAVING count(i.lemma.lemma) = :lemmasCount
            ORDER BY SUM(i.rank * LOG( (SELECT COUNT(p) FROM Page p) * 1.0 / i.lemma.frequency) ) DESC
           """)
    Page<SearchResultDto> searchPagesByLemmasOrderByRelevance(List<String> lemmas, int lemmasCount, Pageable paging, Site site);
}
