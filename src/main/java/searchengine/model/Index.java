package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "`index`")
@Getter
@Setter
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page page;

//    @ManyToOne(cascade = CascadeType.PERSIST)
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;

    @Column(name = "`rank`")
    private Float rank;
}
