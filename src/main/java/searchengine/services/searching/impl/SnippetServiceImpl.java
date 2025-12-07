package searchengine.services.searching.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.common.list.ListUtils;
import searchengine.common.text.HtmlUtils;
import searchengine.common.text.TextUtils;
import searchengine.services.indexing.service.impl.LemmaExtractorServiceImpl;
import searchengine.services.searching.Snippet;
import searchengine.services.searching.SnippetService;
import searchengine.services.searching.model.ContentRange;
import searchengine.services.searching.model.Range;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnippetServiceImpl implements SnippetService {
    private final LemmaExtractorServiceImpl lemmaExtractor;
    private final static int SEARCH_TERMS_RANGE_SPLIT_THRESHOLD = 180;
    private final static int CONTENT_MAX_LENGTH = 220;
    private final static int MAX_WORD_LOOKUP_LENGTH = 100;
    private final static int MAX_SENTENCE_LOOKUP_LENGTH = 100;
    private final static int CONTEXT_WORD_COUNT = 4;
    private final static String BOLD_TAG = "b";
    private final static String SHORT_FRAGMENT_SEPARATOR = " ... ";

    @Override
    public Snippet buildSnippet(String content, List<String> searchTerms) {
        String text = HtmlUtils.stripHtmlTags(content);
        Collection<List<Integer>> searchTermsPositionsList = lemmaExtractor.buildLemmaPositionsMap(text, searchTerms).values();

        if (searchTermsPositionsList.isEmpty())
            return Snippet.of();

        int searchQueryLength = String.join(" ", searchTerms).length();
        List<Integer> searchTermsPositionsFlat = searchTermsPositionsList.stream().flatMap(List::stream).sorted().toList();
        List<ContentRange> contentRanges = mapToContentRanges(searchTermsPositionsList, text, searchQueryLength);
        String snippetContent = prepareSnippetContent(text, contentRanges, searchTermsPositionsFlat);

        return Snippet.of(snippetContent);
    }

    private List<ContentRange> mapToContentRanges(Collection<List<Integer>> searchTermsPositionsCollection, String text, int searchQueryLength) {
        boolean splitWideRanges = searchQueryLength <= SEARCH_TERMS_RANGE_SPLIT_THRESHOLD;

        List<Range> searchTermsRanges = mapSearchTermsRanges(new ArrayList<>(searchTermsPositionsCollection), splitWideRanges);
        sortByTermsRangeRelevance(searchTermsRanges, searchQueryLength);

        List<ContentRange> contentRanges = new ArrayList<>();
        boolean isFirstRange = true;

        for (Range range : searchTermsRanges) {
            ContentRange contentRange = ContentRange.of(range);
            contentRange.setSentenceStart(isFirstRange);
            processContentRange(contentRange, text);
            contentRanges.add(contentRange);
            isFirstRange = false;
        }

        mergeOverlappingRanges(contentRanges);
        trimRangesSummaryLength(contentRanges, text);

        return contentRanges;
    }

    private List<Range> mapSearchTermsRanges(List<List<Integer>> searchTermsPositionsList, boolean splitWideRanges) {
        searchTermsPositionsList.sort(Comparator.comparingInt(List::size));
        List<Range> ranges = new ArrayList<>();
        List<Integer> firstTermPositions = ListUtils.getFirst(searchTermsPositionsList);
        boolean isSingleTermQuery = searchTermsPositionsList.size() == 1;

        if (isSingleTermQuery) {
            ranges.addAll(mapToSingleRanges(firstTermPositions));
            return ranges;
        }

        List<List<Integer>> nextTermsPositionsList = ListUtils.getSubList(searchTermsPositionsList,1);

        for (Integer firstTermPosition : firstTermPositions) {
            List<Integer> positions = new ArrayList<>();
            positions.add(firstTermPosition);
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (List<Integer> nextTermPositions : nextTermsPositionsList) {
                int position = ListUtils.findNearestIntValue(firstTermPosition, nextTermPositions);
                positions.add(position);
                min = Math.min(position, min);
                max = Math.max(position, max);
            }

            Range range = Range.of(min, max);

            if (range.getRange() >= SEARCH_TERMS_RANGE_SPLIT_THRESHOLD && splitWideRanges)
                ranges.addAll(mapToSingleRanges(positions));
            else
                ranges.add(range);
        }

        return ranges;
    }

    private List<Range> mapToSingleRanges(List<Integer> positions) {
        return positions.stream().map(position -> Range.of(position, position)).collect(Collectors.toList());
    }

    private void sortByTermsRangeRelevance(List<Range> contentRanges, int searchQueryLength) {
        contentRanges.sort(
                Comparator.comparingInt(range -> {
                    if (range.getRange() == 0) return range.getStart();
                    return Math.abs(range.getRange() - searchQueryLength);
                })
        );
    }

    private void processContentRange(ContentRange contentRange, String text) {
        int previousContextWordsStart = TextUtils.indexOfPreviousWordsStart(text, contentRange.getStart(), CONTEXT_WORD_COUNT);
        int contentRangeStart = previousContextWordsStart == -1 ? contentRange.getStart() : previousContextWordsStart;

        if (contentRange.isSentenceStart()) {
            int sentenceStartIndex = TextUtils.indexOfSentenceStart(text, contentRange.getStart(), MAX_SENTENCE_LOOKUP_LENGTH);
            contentRangeStart = sentenceStartIndex == -1 ? contentRangeStart : sentenceStartIndex;
            contentRange.setSentenceStart(sentenceStartIndex != -1);
        }

        int lastWordEnd = TextUtils.indexOfWordEnd(text, contentRange.getEnd(), MAX_WORD_LOOKUP_LENGTH);
        int nextContextWordsEnd = TextUtils.indexOfNextWordsEnd(text, contentRange.getEnd(), CONTEXT_WORD_COUNT);
        int contentRangeEnd = nextContextWordsEnd == -1 ? lastWordEnd : nextContextWordsEnd;

        contentRange.setStart(contentRangeStart);
        contentRange.setEnd(contentRangeEnd);
    }

    private void mergeOverlappingRanges(List<ContentRange> contentRanges) {
        List<ContentRange> sortedContentRanges = new ArrayList<>(contentRanges);
        sortedContentRanges.sort(Comparator.comparingInt(ContentRange::getStart));

        List<ContentRange> mergedContentRanges = new ArrayList<>();
        ContentRange current = sortedContentRanges.get(0);
        int counter = 0;

        for (ContentRange comparing : sortedContentRanges) {
            boolean isOutOfRange = current.getEnd() < comparing.getStart();

            if (isOutOfRange) {
                mergedContentRanges.add(current);
                current = comparing;
            }

            current.setEnd(Math.max(current.getEnd(), comparing.getEnd()));

            if (++counter == sortedContentRanges.size())
                mergedContentRanges.add(current);
        }

        contentRanges = mergedContentRanges;
    }

    private void trimRangesSummaryLength(List<ContentRange> contentRanges, String text) {
        List<ContentRange> contentRangesCopy = new ArrayList<>(contentRanges);
        contentRanges.clear();
        int lengthReserve = CONTENT_MAX_LENGTH;

        for (ContentRange contentRange : contentRangesCopy) {
            if (contentRange.getRange() > lengthReserve) {
                int trimIndex = contentRange.getStart() + lengthReserve;
                contentRange.setEnd(TextUtils.indexOfWordEnd(text, trimIndex, MAX_WORD_LOOKUP_LENGTH));
                contentRanges.add(contentRange);
                break;
            }

            contentRanges.add(contentRange);
            lengthReserve -= contentRange.getRange();
        }
    }

    private String prepareSnippetContent(String text, List<ContentRange> contentRanges, List<Integer> searchTermsPositionsFlat) {
        StringBuilder contentBuilder = new StringBuilder();
        boolean isFirstFragment = true;

        for (ContentRange contentRange : contentRanges) {
            int contentRangeStart = contentRange.getStart();
            int contentRangeEnd = contentRange.getEnd();
            String contentRangeText = text.substring(contentRangeStart, contentRangeEnd);
            List<Integer> searchTermsRelativeIndexes = ListUtils.decrease(
                    ListUtils.trimValuesToRange(
                            searchTermsPositionsFlat,
                            contentRangeStart,
                            contentRangeEnd),
                    contentRangeStart,
                    false
            );

            if (!contentRange.isSentenceStart() && isFirstFragment)
                contentBuilder.append(SHORT_FRAGMENT_SEPARATOR);

            contentRangeText = TextUtils.wrapAllWords(
                    contentRangeText,
                    searchTermsRelativeIndexes,
                    (word) -> HtmlUtils.wrapByTag(word, BOLD_TAG)
            );

            contentBuilder.append(contentRangeText);
            contentBuilder.append(SHORT_FRAGMENT_SEPARATOR);
            isFirstFragment = false;
        }

        return contentBuilder.toString();
    }
}
