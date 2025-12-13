package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLInsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "page",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "path", "site_id"
                })
)
@Getter
@Setter
@SQLInsert(sql = "INSERT INTO page (code,content,path,site_id) values (?, ?, ?, ?) ON CONFLICT (site_id, path) DO NOTHING")
public class Page implements Comparable<Page> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="path", columnDefinition = "TEXT")
    private String path;

    @Column(name="code")
    private Integer code;

    @Column(name="content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<Index> indexList = new ArrayList<>();

    public void addIndex(Index index) {
        index.setPage(this);
        this.indexList.add(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(getPath(), page.getPath()) && Objects.equals(site.getId(), page.site.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), site.getId());
    }

    @Override
    public int compareTo(Page p) {
        return (p.getPath() + p.site.getId())
                .compareTo(this.getPath() + this.site.getId());
    }
}
