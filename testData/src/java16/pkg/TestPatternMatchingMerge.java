package pkg;

public class TestPatternMatchingMerge {
    public void test(Object obj) {
        String s = "hi";

        if (obj instanceof String) {
            s = (String) obj;
        }
    }

    public void testNoInit(Object obj) {
        String s = null;
        if (obj instanceof String) {
            s = (String) obj;
        }
      System.out.println(s);
    }
}
