package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {
    public static String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String extractEntryPointPath(String baseUrl) {
        if (baseUrl.matches("^https+://[^/]+/?$"))
            return "/";

        return baseUrl.replaceAll("^https?://[^/]+/", "/");
    }

    public static String extractBaseUrl(String url) {
        String regex = "^(https?://[^/]+)/?.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) return removeTrailingSlash(matcher.group(1));
        else return "";
    }

    public static String toRelativePath(String url, String baseUrl) {
        String regex = "https?://|" + stripUrl(baseUrl);
        url = url.replaceAll(regex, "");
        return url;
    }

    public static String stripUrl(String url) {
        return url.replaceAll("https?://", "");
    }

    public static boolean isValidLink(String url, String baseUrl) {
        return !(isInvalidLink(url) || isExternalLink(url, baseUrl) || isRootPath(url));
    }

    public static boolean isExternalLink(String url, String baseUrl) {
        return !isAbsoluteLink(url, baseUrl) && !isPath(url);
    }

    public static boolean isInvalidLink(String url) {
        String regex = "^mailto.*|.*#.*";
        return url.matches(regex);
    }

    public static boolean isAbsoluteLink(String url, String baseUrl) {
        return url.contains(stripUrl(baseUrl));
    }

    public static boolean isPath(String url) {
        String regex = "^/[^/].*$";
        return url.matches(regex);
    }

    public static boolean isRootPath(String url) {
        return url.equals("/");
    }
}