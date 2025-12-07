package searchengine.services.mapper;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Comparator;
import java.util.Map;

public class LemmaIndexMapper {
    public static void mapToEntities(Page page, Site site, Map<String, Integer> lemmasFrequencyMap) {
        Integer maxLemmaFrequency = lemmasFrequencyMap.values().stream()
                .max(Comparator.comparing(Integer::intValue)).orElse(1);

        lemmasFrequencyMap.forEach((word, frequency) -> {
            Lemma lemma = new Lemma();
            lemma.setSite(site);
            lemma.setLemma(word);
            lemma.setFrequency(1);

            Index index = new Index();
            index.setPage(page);

            Float rank = (float) frequency / maxLemmaFrequency;
            index.setRank(rank);

            lemma.addIndex(index);
            page.addIndex(index);
        });
    }
}
