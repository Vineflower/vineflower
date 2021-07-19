package pkg;

public class TestMultiAssignmentInStaticBlock {
    private static int i = 124151;
    private static final int a;
    private static final int b;
    private static final int c;
    private static final int d;
    private static final int e;

    static {
        a = (i * 11);
        i = (i * 14151 + 151) ^ 414;
        b = (i * 11);
        i = (i * 14151 + 151) ^ 414;
        c = (i * 11);
        i = (i * 14151 + 151) ^ 414;
        d = (i * 11);
        i = (i * 14151 + 151) ^ 414;
        e = (i * 11);
        i = (i * 14151 + 151) ^ 414;
    }
}
