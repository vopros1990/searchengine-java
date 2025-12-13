package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLInsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "lemma",
        uniqueConstraints = @UniqueConstraint(
        columnNames = {
                "lemma", "site_id"
        }))
@Getter
@Setter
@SQLInsert(sql = "INSERT INTO lemma (frequency, lemma, site_id) values (?, ?, ?) ON CONFLICT (lemma, site_id) DO UPDATE SET frequency = lemma.frequency + 1")
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(name = "lemma", columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column(name = "frequency")
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE)
    private List<Index> indexList = new ArrayList<>();

    public void addIndex(Index index) {
        index.setLemma(this);
        this.indexList.add(index);
    }

    @Override
    public int compareTo(Lemma lemma) {
        return this.getLemma().compareTo(lemma.getLemma());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(getSite(), lemma1.getSite()) && Objects.equals(getLemma(), lemma1.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSite(), getLemma());
    }
}
