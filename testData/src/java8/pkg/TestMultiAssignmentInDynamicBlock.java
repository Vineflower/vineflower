package pkg;

public class TestMultiAssignmentInDynamicBlock {
    private int i = 124151;
    private final int a;
    private final int b;
    private final int c;
    private final int d;
    private final int e;

    {
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
