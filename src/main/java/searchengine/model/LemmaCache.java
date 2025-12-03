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
                        "`word`"
                }))
public class LemmaCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="word", columnDefinition = "VARCHAR(60)")
    private String word;

    @Column(name="lemma", columnDefinition = "VARCHAR(60)")
    private String lemma;
}
