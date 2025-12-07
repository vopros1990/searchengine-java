package searchengine.common.text;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TextUtils {
    public final static String WORD_SEPARATOR_REGEX = "[^a-zA-Zа-яА-ЯёЁ0-9-]+";
    public final static String WORD_REGEX = "[a-zA-Zа-яА-ЯёЁ0-9-]+";
    public final static String SENTENCE_BEGIN_REVERSE_REGEX = "[A-ZА-ЯЁ]$|[A-ZА-ЯЁ]\\s+[.!?]|[A-ZА-ЯЁ][.!?]";

    public final static String CYRILLIC_WORD_REGEX = "[а-яА-Я-]+";
    public final static String LATIN_WORD_REGEX = "[a-zA-Z-]+";

    public static List<String> extractWords(String text) {
        if (text == null) return List.of();

        return Arrays.stream(
                text.toLowerCase().trim()
                        .split(WORD_SEPARATOR_REGEX))
                .filter(TextUtils::isWord)
                .toList();
    }

    public static boolean isCyrillicWord(String word) {
        return word.matches(CYRILLIC_WORD_REGEX);
    }

    public static boolean isLatinWord(String word) {
        return word.matches(LATIN_WORD_REGEX);
    }

    public static boolean isWord(String word) {
        return word.matches(WORD_REGEX);
    }

    public static int indexOfSentenceStart(String text, int referenceIndex, int lookupLimit) {
        if (referenceIndex == 0) return 0;

        int startIndex = text.length() < lookupLimit ? 0 : Math.max(referenceIndex, lookupLimit) - lookupLimit;
        String reversedText = reverse(text.substring(startIndex, referenceIndex));
        Matcher matcher = Pattern.compile(SENTENCE_BEGIN_REVERSE_REGEX).matcher(reversedText);
        if (!matcher.find()) return -1;
        return referenceIndex - matcher.start() - 1;
    }

    public static int indexOfNextWordsEnd(String text, int referenceIndex, int wordsCountLimit) {
        if (text.substring(referenceIndex).matches("^" + WORD_REGEX + ".*")) wordsCountLimit++;

        String processingText = referenceIndex == 0 ? text : text.substring(referenceIndex);
        Matcher matcher = Pattern.compile(WORD_REGEX).matcher(processingText);
        List<MatchResult> matchResults = matcher.results().toList();

        if (matchResults.isEmpty()) return referenceIndex;

        wordsCountLimit = Math.min(matchResults.size(), wordsCountLimit);
        return referenceIndex + matchResults.get(wordsCountLimit - 1).end();
    }

    public static int indexOfPreviousWordsStart(String text, int referenceIndex, int wordsCountLimit) {
        if (referenceIndex == 0) return 0;

        return referenceIndex - indexOfNextWordsEnd(
                reverse(text.substring(0, referenceIndex)),
                0,
                wordsCountLimit
        );
    }

    public static int indexOfWordEnd(String text, int referenceIndex, int lookupLimit) {
        int endIndex = text.length() < lookupLimit + referenceIndex ? text.length() - 1 : lookupLimit + referenceIndex;

        Matcher matcher = Pattern.compile(WORD_SEPARATOR_REGEX).matcher(
                text.substring(referenceIndex, endIndex));

        return (matcher.find()) ? matcher.start() + referenceIndex : text.length() - 1;
    }

    public static int indexOfWordStart(String text, int referenceIndex, int lookupLimit) {
        if (referenceIndex == 0) return 0;

        return referenceIndex - indexOfWordEnd(
                reverse(text.substring(0, referenceIndex)),
                0,
                lookupLimit
        );
    }

    public static String wrapAllWords(String text, List<Integer> wordsIndexList, Function<String, String> wrapper) {
        if (wordsIndexList.isEmpty()) return text;

        Matcher matcher = Pattern.compile(WORD_REGEX).matcher(text);
        StringBuilder builder = new StringBuilder();
        int startIndex = 0;

        for (Integer wordIndex : wordsIndexList) {
            if (wordIndex > text.length()) continue;

            builder.append(text, startIndex, wordIndex);

            if (matcher.find(wordIndex)) {
                String wrappedWord = wrapper.apply(matcher.group());
                builder.append(wrappedWord);
                startIndex = matcher.end();
            }
        }
        builder.append(text, startIndex, text.length());

        return builder.toString();
    }

    public static String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }
}
