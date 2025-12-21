package searchengine.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {
    private static final String HTML_TAGS_AND_COMMENTS_REGEX =
            "<style>[\\s\\S]*?</style>|<(no)?script[^>]*>[\\s\\S]*?</(no)?script>|<!--.*?-->|<!--[\\S\\s]+?-->|<!--[\\S\\s]*?$|</?[^>]+>";

    public static String stripHtmlTags(String content) {

        if (content == null || content.isBlank()) {
            return "";
        }

        String text = content
                .replace("\u00AD", "")
                .replaceAll("[\\p{Cf}]", "")
                .replace('\u00A0', ' ');

        text = text.replaceAll(HTML_TAGS_AND_COMMENTS_REGEX, " ");
        text = text.replaceAll("\\s+", " ");

        return text.trim();
    }

    public static String getTagContent(String tag, String htmlContent) {
        String regex = String.format("<%s>([^<]+)</%s>", tag, tag);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find())
            return matcher.group(1);

        return null;
    }

    public static String wrapByTag(String text, String tag) {
        return String.format("<%s>%s</%s>", tag, text, tag);
    }


}
