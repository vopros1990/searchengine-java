package searchengine.common.text;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {
    private static final String HTML_TAGS_AND_COMMENTS_REGEX =
            "<style>[\\s\\S]*?</style>|<(no)?script[^>]*>[\\s\\S]*?</(no)?script>|<!--.*?-->|<!--[\\S\\s]+?-->|<!--[\\S\\s]*?$|</?[^>]+>";

    public static String stripHtmlTags(String content) {
        String text = content.replaceAll(HTML_TAGS_AND_COMMENTS_REGEX, "");
        text = text.replaceAll("\\s+|&nbsp;", " ");
        return text;
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
