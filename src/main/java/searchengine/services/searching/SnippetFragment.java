package searchengine.services.searching;

import lombok.Getter;
import lombok.Setter;
import searchengine.common.list.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class SnippetFragment {
    private Set<Integer> searchTermsPositionsSet = new TreeSet<>();
    private int termsRangeEnd;
    private int contentRangeStart;
    private int contentRangeEnd;
    boolean hasSentenceStart;

    public static SnippetFragment of(List<Integer> searchTermsPositionsList) {
        SnippetFragment fragment = new SnippetFragment();
        fragment.addAllIndexes(searchTermsPositionsList);
        fragment.termsRangeEnd = fragment.contentRangeEnd = ListUtils.maxValue(searchTermsPositionsList);
        fragment.contentRangeStart = fragment.getTermsRangeStart();
        fragment.hasSentenceStart = false;
        return fragment;
    }

    private SnippetFragment() {
    }

    public int getTermsRangeStart() {
        return ListUtils.minValue(searchTermsPositionsSet);
    }

    public int getTermsRange() {
        return termsRangeEnd - getTermsRangeStart();
    }

    public int getContentRange() {
        return contentRangeEnd - contentRangeStart;
    }

    public void addAllIndexes(List<Integer> indexList) {
        searchTermsPositionsSet.addAll(indexList);
    }

    public void replaceAllIndexes(List<Integer> indexList) {
        searchTermsPositionsSet.clear();
        searchTermsPositionsSet.addAll(indexList);
    }

    public void addIndexesToContentRange(List<Integer> indexList) {
        searchTermsPositionsSet.addAll(ListUtils.trimValuesToRange(indexList, contentRangeStart, contentRangeEnd));
    }

    public int searchTermPositionCount() {
        return searchTermsPositionsSet.size();
    }

    public List<Integer> getSearchTermsPositionsList() {
        return new ArrayList<>(searchTermsPositionsSet);
    }

    public List<Integer> getSearchTermsRelativePositionsList(int startIndex) {
        return searchTermsPositionsSet.stream().filter(i -> i >= startIndex).map(i -> i - startIndex).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "SnippetFragment{" +
                "searchTermsPositionsSet=" + searchTermsPositionsSet +
                ", termsRangeEnd=" + termsRangeEnd +
                ", contentRangeStart=" + contentRangeStart +
                ", contentRangeEnd=" + contentRangeEnd +
                ", hasSentenceStart=" + hasSentenceStart +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnippetFragment that = (SnippetFragment) o;
        return getTermsRangeStart() == that.getTermsRangeStart() && getTermsRangeEnd() == that.getTermsRangeEnd() && getTermsRange() == that.getTermsRange();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTermsRangeStart(), getTermsRangeEnd(), getTermsRange());
    }
}
