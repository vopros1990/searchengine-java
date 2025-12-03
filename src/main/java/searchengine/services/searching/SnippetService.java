package searchengine.services.searching;

import java.util.List;

public interface SnippetService {
    Snippet buildSnippet(String content, List<String> searchTerms);
}
