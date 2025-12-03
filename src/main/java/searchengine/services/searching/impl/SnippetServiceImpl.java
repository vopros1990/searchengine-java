package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.common.list.ListUtils;
import searchengine.common.text.HtmlUtils;
import searchengine.common.text.TextUtils;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.services.searching.Snippet;
import searchengine.services.searching.SnippetFragment;
import searchengine.services.searching.SnippetService;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetServiceImpl implements SnippetService {
    private final LemmaExtractorServiceImpl lemmaExtractor;
    private final static int SEARCH_TERMS_MAX_RANGE = 200;
    private final static int CONTENT_MAX_RANGE = 250;
    private final static int MAX_WORD_LENGTH = 100;
    private final static int MAX_SENTENCE_LOOKUP_LENGTH = 100;
    private final static int CONTEXT_WORD_COUNT = 4;
    private final static String HIGHLIGHT_TAG = "b";
    private final static String SHORT_FRAGMENT_SEPARATOR = " <...> ";

    @Override
    public Snippet buildSnippet(String content, List<String> searchTerms) {
        String text = HtmlUtils.stripHtmlTags(content);
        Map<String, List<Integer>> lemmaPositionsMap = lemmaExtractor.buildLemmaPositionsMap(text, searchTerms);

        if (lemmaPositionsMap.isEmpty())
            return Snippet.of();

        List<SnippetFragment> snippetFragments = lemmaPositionsMap.size() == 1 ?
                mapSingleTermRange(ListUtils.getFirst(lemmaPositionsMap.values())) :
                mapMultiTermRange(lemmaPositionsMap.values());

        mapContentRange(snippetFragments, text, CONTENT_MAX_RANGE);

        return processSnippet(text, snippetFragments);
    }

    private int mapContentRange(List<SnippetFragment> snippetFragments, String text, int contentMaxRange) {
        List<SnippetFragment> mappedFragments = new ArrayList<>();
        snippetFragments.sort(Comparator.comparingInt(SnippetFragment::getTermsRange));
        int reserve = contentMaxRange;

        for (SnippetFragment fragment : snippetFragments) {

            if (fragment.getTermsRange() > SEARCH_TERMS_MAX_RANGE) {
                List<SnippetFragment> singleTermFragments = splitToSingleTermFragments(fragment);
                reserve -= mapContentRange(singleTermFragments, text, reserve);
                mappedFragments.addAll(singleTermFragments);
                continue;
            }

            mapFragmentContentRange(fragment, text, snippetFragments.indexOf(fragment) == 0);

            if (fragment.getContentRange() > reserve) break;

            mappedFragments.add(fragment);
            reserve -= fragment.getContentRange();
        }

        mappedFragments.sort(Comparator.comparingInt(SnippetFragment::getContentRangeStart));
        snippetFragments.clear();
        snippetFragments.addAll(mappedFragments);

        return contentMaxRange - reserve;
    }

    private List<SnippetFragment> splitToSingleTermFragments(SnippetFragment fragment) {
        List<SnippetFragment> result = new ArrayList<>(fragment.searchTermPositionCount());

        for (Integer termStart : fragment.getSearchTermsPositionsSet()) {
            result.add(SnippetFragment.of(List.of(termStart)));
        }

        return result;
    }

    private void mapFragmentContentRange(SnippetFragment fragment, String text, boolean addSentenceStart) {
        int firstTermStart = fragment.getTermsRangeStart();
        int previousWordsStartIndex = TextUtils.indexOfPreviousWordsStart(text, firstTermStart, CONTEXT_WORD_COUNT);
        int contentRangeStart = previousWordsStartIndex == -1 ? firstTermStart : previousWordsStartIndex;

        if (addSentenceStart) {
            int sentenceStartIndex = TextUtils.indexOfSentenceStart(text, firstTermStart, MAX_SENTENCE_LOOKUP_LENGTH);
            contentRangeStart = sentenceStartIndex == -1 ? contentRangeStart : sentenceStartIndex;
            addSentenceStart = sentenceStartIndex != -1;
        }

        int lastTermStart = fragment.getTermsRangeEnd();
        int lastTermEnd = TextUtils.indexOfWordEnd(text, lastTermStart, MAX_WORD_LENGTH);

        int contentRangeEnd = Math.max(
                lastTermEnd,
                TextUtils.indexOfNextWordsEnd(text, lastTermEnd, CONTEXT_WORD_COUNT)
        );

        fragment.setTermsRangeEnd(lastTermEnd);
        fragment.setHasSentenceStart(addSentenceStart);
        fragment.setContentRangeStart(contentRangeStart);
        fragment.setContentRangeEnd(contentRangeEnd);
    }

    private Snippet processSnippet(String text, List<SnippetFragment> snippetFragments) {
        Snippet snippet = new Snippet();

        int searchTermsMinRange = snippetFragments.stream()
                .min(Comparator.comparingInt(SnippetFragment::getTermsRange)).orElse(SnippetFragment.of(List.of(1, 2)))
                .getTermsRange();

        snippet.setSearchTermsMinRange(searchTermsMinRange);
        snippetFragments.sort(Comparator.comparingInt(SnippetFragment::getContentRangeStart));
        snippet.setContent(prepareSnippetContent(text, snippetFragments));

        return snippet;
    }

    private String prepareSnippetContent(String text, List<SnippetFragment> snippetFragments) {
        StringBuilder contentBuilder = new StringBuilder();

        for (SnippetFragment fragment : snippetFragments) {
            int contentRangeStart = fragment.getContentRangeStart();
            int contentRangeEnd = fragment.getContentRangeEnd();
            int termsRangeStart = fragment.getTermsRangeStart();
            int termsRangeEnd = fragment.getTermsRangeEnd();

            boolean hasSentenceStart = fragment.isHasSentenceStart();

            if (!hasSentenceStart && snippetFragments.indexOf(fragment) == 0)
                contentBuilder.append(SHORT_FRAGMENT_SEPARATOR);

            contentBuilder.append(text, contentRangeStart, termsRangeStart);

            String termsRangeContent = text.substring(termsRangeStart, termsRangeEnd);

            termsRangeContent = TextUtils.wrapAllWords(
                    termsRangeContent,
                    fragment.getSearchTermsRelativePositionsList(termsRangeStart),
                    (word) -> HtmlUtils.wrapByTag(word, HIGHLIGHT_TAG)
            );

            contentBuilder.append(termsRangeContent);
            contentBuilder.append(text, termsRangeEnd, contentRangeEnd);
            contentBuilder.append(SHORT_FRAGMENT_SEPARATOR);
        }

        return contentBuilder.toString();
    }

    private List<SnippetFragment> mapSingleTermRange(List<Integer> termPositions) {
        Collections.sort(termPositions);

        List<SnippetFragment> snippetFragments = new ArrayList<>();
        snippetFragments.add(
                SnippetFragment.of(termPositions)
        );

        return snippetFragments;
    }

    private List<SnippetFragment> mapMultiTermRange(Collection<List<Integer>> termPositions) {
        List<Integer> firstTermPositions = ListUtils.getFirst(termPositions);
        List<List<Integer>> nextTermsPositionsList = ListUtils.getSubList(termPositions,1);
        List<SnippetFragment> snippetFragments = new ArrayList<>();

        for (Integer firstTermPosition : firstTermPositions) {
            List<Integer> searchTermsPositionsList = new ArrayList<>();
            searchTermsPositionsList.add(firstTermPosition);

            for (List<Integer> nextTermPositions : nextTermsPositionsList)
                searchTermsPositionsList.add(ListUtils.findNearestIntValue(firstTermPosition, nextTermPositions));

            SnippetFragment fragment = SnippetFragment.of(searchTermsPositionsList);

            termPositions.forEach(list -> fragment.addAllIndexes(
                    ListUtils.trimValuesToRange(
                            list,
                            fragment.getTermsRangeStart(),
                            fragment.getTermsRangeEnd())
            ));

            snippetFragments.add(fragment);
        }

        return snippetFragments;
    }
}
