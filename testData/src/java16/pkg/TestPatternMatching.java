package java16;

public class TestPatternMatching {
    public void testSimple(Object obj) {
        if (obj instanceof String str) {
            System.out.println(str.length());
        }
    }

    public void testCompound(Object obj) {
        if (obj instanceof String str && str.contains("hi")) {
            System.out.println(str.length());
        }
    }

    public boolean testReturn(Object obj) {
        return obj instanceof String s && s.length() > 5;
    }

    public int testReturnTernary(Object obj) {
        return obj instanceof String s ? s.length() : 0;
    }

    public int testReturnTernaryComplex(Object obj) {
        return obj instanceof String s && s.length() > 5 || obj instanceof Integer ? 4 : 1;
    }

    public void testLoop(Object obj) {
        while (obj instanceof String s && s.length() > 10) {
            s = s.substring(1);
            obj = s.substring(1);

            System.out.println(s);
        }
    }
}
