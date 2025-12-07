package searchengine.services.searching.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Range {
    private int start;
    private int end;

    public static Range of(int start, int end) {
        Range range = new Range();
        range.start = start;
        range.end = end;

        return range;
    }

    public int getRange() {
        return end - start;
    }

    @Override
    public String toString() {
        return "<br>Range{<br>" +
                "start=" + start +
                ", <br>end=" + end +
                "} <br>";
    }


}
