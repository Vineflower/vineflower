package records;

public class TestRecordInner {
    private record Rec(int x) {}

    public Rec create(int x) {
        return new Rec(x);
    }
}
