package java16;

public class TestPatternMatchingFake {
    public void test1(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer)obj;
            System.out.println(1 + i);
        }

        if (obj instanceof Integer i) {
            System.out.println(1 + i);
        }
    }

    public void test1A(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer)obj;
            System.out.println(1 + i);
        }
    }

    public void test1B(Object obj) {
        if (obj instanceof Integer i) {
            System.out.println(1 + i);
        }
    }

    public void test2(Object obj) {
        if (obj instanceof Integer) {
            System.out.println(1 + (Integer)obj);
        }

        if (obj instanceof Integer i) {
            System.out.println(1 + i);
        }
    }

    public void testClash(Object obj) {
        if (!(obj instanceof String s)) {
            int s = 0;

            System.out.println(s);
        } else {
            System.out.println(s.length());
        }
    }
}
