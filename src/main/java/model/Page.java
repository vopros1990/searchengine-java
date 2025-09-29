package model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "page")
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name="path", columnDefinition = "TEXT")
    private String path;

    @Column(name="code")
    private int code;

    @Column(name="content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL)
    private List<Index> indexList;
}
