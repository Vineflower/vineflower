package pkg;

public record TestRecordMixup(int x, int y) {
    public TestRecordMixup(int x, int y) {
        this.x = y;
        this.y = x;
    }

    @Override
    public int x() {
        return this.y;
    }

    @Override
    public int y() {
        return this.x;
    }
}
