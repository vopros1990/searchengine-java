package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLInsert;

@Entity
@Getter
@Setter
@Table(name = "lemma_cache",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "word"
                }))
@SQLInsert(sql = "INSERT INTO lemma_cache (word, lemma) values (?, ?) ON CONFLICT (word) DO NOTHING")
public class LemmaCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="word", columnDefinition = "VARCHAR(255)")
    private String word;

    @Column(name="lemma", columnDefinition = "VARCHAR(255)")
    private String lemma;
}
