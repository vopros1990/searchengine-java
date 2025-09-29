package model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
    private int id;

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

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<Page> pageList;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<Lemma> lemmaList;
}