package searchengine.common.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class URLUtilsTest {
    @Test
    @DisplayName("Return correct entry point path")
    public void givenUrlWithEntryPoint_whenExtractEntryPointPath_thenReturnCorrectEntryPointPath() {
        String url = "https://example.com/entry_point_path";
        String expected = "/entry_point_path";
        String actual = URLUtils.extractEntryPointPath(url);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Return correct relative path when base url has no trailing slash")
    public void givenUrlAndBaseUrlWithoutTrailingSlash_whenToRelativePath_thenReturnPath() {
        String url = "https://example.com/relative/path/to";
        String baseUrl = "https://example.com";
        String expected = "/relative/path/to";
        String actual = URLUtils.toRelativePath(url, baseUrl);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Return correct relative path when base url has trailing slash")
    public void givenUrlAndBaseurlWithTrailingSlash_whenToRelativePath_thenReturnPath() {
        String url = "https://example.com/relative/path/to";
        String baseUrl = "https://example.com/";
        String expected = "/relative/path/to";
        String actual = URLUtils.toRelativePath(url, baseUrl);
        assertEquals(expected, actual);
    }
}
