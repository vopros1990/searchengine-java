package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

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
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="path", columnDefinition = "TEXT")
    private String path;

    @Column(name="site_id")
    private Integer siteId;

    @Column(name="code")
    private Integer code;

    @Column(name="content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<Index> indexList;

    public boolean isEmpty() {
        return path == null && siteId == null && code == null && content == null && site == null && indexList == null;
    }
}
