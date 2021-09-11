package java16;

public class TestPatternMatchingMerge {
    public void test(Object obj) {
        String s = "hi";

        if (obj instanceof String) {
            s = (String) obj;
        }
    }

    public void testNoInit(Object obj) {
        String s;
        if (obj instanceof String) {
            s = (String) obj;
        }
    }
}
