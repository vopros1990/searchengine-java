package searchengine.services.searching.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentRange extends Range {
    private boolean sentenceStart;

    public ContentRange() {
        super();
    }

    public static ContentRange of(Range range) {
        ContentRange contentRange = new ContentRange();
        contentRange.setStart(range.getStart());
        contentRange.setEnd(range.getEnd());

        return contentRange;
    }

    public static ContentRange of(int start, int end) {
        ContentRange contentRange = new ContentRange();
        contentRange.setStart(start);
        contentRange.setEnd(end);

        return contentRange;
    }

    @Override
    public String toString() {
        return "<br>ContentRange:<br>" +
                "sentenceStart=" + sentenceStart +
                "<br> " +
                "start=" + this.getStart() +
                ", end=" + this.getEnd() +
                ", <br>range=" + this.getRange();
    }
}
