package searchengine.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class LemmaExtractorTest {
    @Autowired
    private LemmaExtractorServiceImpl lemmaExtractor;

    @Test
    @DisplayName("Test returning an empty Map when parsing empty content")
    public void whenEmptyContent_ThenReturnEmptyMap() {
        String content = "";
        Map<String, Integer> expected = new TreeMap<>();
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test returning an empty Map when parsing content with no words")
    public void whenNoWordsContent_ThenReturnEmptyMap() {
        String content = "<, ?, ;72387ad,%3452#$@%    /" + System.lineSeparator();
        Map<String, Integer> expected = new TreeMap<>();
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test correct processing cyryllic words")
    public void whenCyrillicContent_ThenReturnBaseWordForms() {
        String content = "стихотворной формы";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("стихотворный", 1);
        expected.put("форма", 1);
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test ignoring case in cyryllic words")
    public void whenUpperCaseCyrillicContent_ThenReturnBaseWordForms() {
        String content = "сТихотвОрной фоРмы";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("стихотворный", 1);
        expected.put("форма", 1);
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test correct word frequency count in cyryllic content")
    public void whenCyrillicContent_ThenReturnCorrectFrequencyCount() {
        String content = "дождь дождя дождю дождливый дождливому";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("дождь", 3);
        expected.put("дождливый", 2);
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test correct processing latin words")
    public void whenLatinContent_ThenReturnBaseWordForms() {
        String content = "Lorem ipsum";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("lorem", 1);
        expected.put("ipsum", 1);
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test returning initial word if neither cyrillic nor latin word given")
    public void whenNeitherCyrillicNorLatinWord_ThenReturnInitialWord() {
        String content = "abraкадабра";
        Map<String, Integer> expected = new TreeMap<>();
        expected.put("abraкадабра", 1);
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Test returning empty map if particle given")
    public void whenParticle_ThenReturnEmptyMap() {
        String content = "ой в и the and";
        Map<String, Integer> expected = new TreeMap<>();
        Map<String, Integer> actual = lemmaExtractor.buildLemmaFrequencyMap(content);
        assertEquals(expected, actual);
    }

}
