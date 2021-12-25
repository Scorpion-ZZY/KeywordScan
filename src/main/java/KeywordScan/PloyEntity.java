package KeywordScan;

public class PloyEntity {
    private boolean top100;
    private boolean isLast100;
    private boolean all;
    private boolean isCustomize;
    private int start;
    private int end;


    public boolean isTop100() {
        return top100;
    }

    public void setTop100(boolean top100) {
        this.top100 = top100;
    }

    public boolean isLast100() {
        return isLast100;
    }

    public void setLast100(boolean last100) {
        isLast100 = last100;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isCustomize() {
        return isCustomize;
    }

    public void setCustomize(boolean customize) {
        isCustomize = customize;
    }
}
