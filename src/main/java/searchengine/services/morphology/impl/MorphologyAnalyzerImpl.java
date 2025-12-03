package searchengine.services.morphology.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.common.text.TextUtils;
import searchengine.services.morphology.MorphologyAnalyzer;
import searchengine.services.morphology.MorphologyCachingService;

import java.io.IOException;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class MorphologyAnalyzerImpl implements MorphologyAnalyzer {
    private final MorphologyCachingService cache;
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ARTICLE", "CONJ"};

    @Override
    public String getWordNormalForm(String word) {
        String wordNormalForm;

        if ((wordNormalForm = getCached(word)) != null)
            return wordNormalForm.equals("#particle") ? null : wordNormalForm;

        try {
            LuceneMorphology morphology;

            if (TextUtils.isLatinWord(word)) morphology = new EnglishLuceneMorphology();
            else if (TextUtils.isCyrillicWord(word)) morphology = new RussianLuceneMorphology();
            else return saveToCacheAndGet(word, word);

            if (morphology.getMorphInfo(word).stream().anyMatch(this::isParticle)) {
                cache.saveWordLemma(word, "#particle");
                return null;
            }

            return saveToCacheAndGet(word, morphology.getNormalForms(word).get(0));
        } catch (IOException e) {
            log.info(e.getMessage());
        }

        return null;
    }

    private String getCached(String word) {
        return cache.getWordLemma(word);
    }

    private String saveToCacheAndGet(String word, String lemma) {
        cache.saveWordLemma(word, lemma);
        return lemma;
    }

    private boolean isParticle(String wordMorphInfo) {
        for (String particle : particlesNames) {
            if (wordMorphInfo.toUpperCase(Locale.ROOT).contains(particle)) return true;
        }

        return false;
    }
}
