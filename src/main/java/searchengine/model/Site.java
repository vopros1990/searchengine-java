package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('INDEXING', 'INDEXED', 'FAIL')", name = "status")
    private IndexingStatus status;

    @Column(name="status_time")
    private LocalDateTime statusTime;

    @Column(name="last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name="url", columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name="name", columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Page> pageList;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Lemma> lemmaList;
}