package searchengine.common.text;

import java.util.*;

public class TextUtils {
    public static List<String> extractWords(String content) {
        if (content == null) return List.of();

        return Arrays.stream(content.split("\s"))
                .filter(TextUtils::isWord)
                .map(String::toLowerCase)
                .toList();
    }

    public static List<String> extractWordsFromHtml(String htmlContent) {
        if (htmlContent == null) return List.of();
        return extractWords(
                stripHtmlTags(htmlContent)
        );
    }

    public static String stripHtmlTags(String content) {
        String regex = "<[^>]*>";
        return content.replaceAll(regex, "");
    }

    public static boolean isCyrillicWord(String word) {
        return word.matches("[а-яА-Я]+");
    }

    public static boolean isLatinWord(String word) {
        return word.matches("[a-zA-Z]+");
    }

    public static boolean isWord(String word) {
        return word.matches("[a-zA-Zа-яА-Я]+");
    }
}
