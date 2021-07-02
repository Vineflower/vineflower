package pkg;

public class TestStaticBlockNull {
    static final String a;
    static final String b;

    static {
        final String s = null;
        a = b = s;
    }
}
