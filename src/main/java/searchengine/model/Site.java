package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "site",
        uniqueConstraints = @UniqueConstraint(
        columnNames = {
                "url"
        }))
@Getter
@Setter
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(16)", name = "status")
    private IndexingStatus status;

    @Column(name="status_time")
    private Instant statusTime;

    @Column(name="last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name="url", columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name="name", columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Page> pageList;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Lemma> lemmaList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return Objects.equals(getId(), site.getId()) && Objects.equals(getUrl(), site.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUrl());
    }
}