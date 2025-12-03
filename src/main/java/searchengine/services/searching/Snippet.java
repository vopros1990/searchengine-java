package searchengine.services.searching;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Snippet {
    private String content;
    private int searchTermsMinRange;

    public static Snippet of(String content) {
        Snippet snippet = new Snippet();
        snippet.setContent(content);

        return snippet;
    }

    public static Snippet of() {
        Snippet snippet = new Snippet();
        snippet.setContent("");

        return snippet;
    }
}
