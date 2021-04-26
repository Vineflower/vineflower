package pkg;

public class TestBoxingConstructor {
    private int x;
    private int y;

    public TestBoxingConstructor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public TestBoxingConstructor(Object x, Object y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public TestBoxingConstructor(Object x, Object y, boolean marker) {
        this(x, y);
    }

    public TestBoxingConstructor(Object x, Object y, boolean marker, boolean marker2) {
        this((int) x, (int) y);
    }
}
