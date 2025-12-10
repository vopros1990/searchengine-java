package searchengine.common.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextUtilsTest {
    @Test
    @DisplayName("Test return correct index when text fragment starts with new sentence")
    public void givenTextStartsWithUppercaseLetter_whenIndexOfSentenceStartIndex_ThenReturnCorrect() {
        String content = "Тестовое предложение";
        int innerIndex = 10;
        int expectedIndex = 0;
        int actualIndex = TextUtils.indexOfSentenceStart(content, innerIndex, 500);
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    @DisplayName("Test return correct index when sentence does not start at line beginning")
    public void givenNewSentenceInsideText_whenIndexOfSentenceStartIndex_ThenReturnCorrect() {
        String content = "... кусочек другого текста. Тестовое предложение";
        int innerIndex = 37;
        int expectedIndex = 28;
        int actualIndex = TextUtils.indexOfSentenceStart(content, innerIndex, 500);
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    @DisplayName("Test return nearest sentence beginning index when text contains two sentences")
    public void givenTwoSentencesInsideText_whenIndexOfSentenceStartIndex_ThenReturnCorrect() {
        String content = "... кусочек другого текста. Первое предложение. Тестовое предложение";
        int innerIndex = 57;
        int expectedIndex = 48;
        int actualIndex = TextUtils.indexOfSentenceStart(content, innerIndex, 500);
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    @DisplayName("Test return correct index when sentence starts after line break")
    public void givenSentenceAtNewLine_whenIndexOfSentenceStartIndex_ThenReturnCorrect() {
        String content = "... кусочек другого текста. Первое предложение." + System.lineSeparator() + "Тестовое предложение";
        int innerIndex = 57;
        int expectedIndex = 48;
        int actualIndex = TextUtils.indexOfSentenceStart(content, innerIndex, 500);
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    @DisplayName("Test return correct index when sentence starts next to point without space")
    public void givenPointDividerWithoutSpace_whenIndexOfSentenceStartIndex_ThenReturnCorrect() {
        String content = "... кусочек другого текста.Тестовое предложение";
        int innerIndex = 37;
        int expectedIndex = 27;
        int actualIndex = TextUtils.indexOfSentenceStart(content, innerIndex, 500);
        assertEquals(expectedIndex, actualIndex);
    }
}
