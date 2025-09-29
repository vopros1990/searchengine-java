package model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "lemma")
@Getter
@Setter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column(name = "frequency")
    private int frequency;

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL)
    private List<Index> indexList;
}
