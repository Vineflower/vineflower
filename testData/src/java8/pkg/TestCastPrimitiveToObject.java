package pkg;

public class TestCastPrimitiveToObject {
    private static int i = 0;
    private static Object o = null;

    public void test() {
        o = (long)i;
        System.out.println(o.getClass());
        System.out.println(o);

        o = i;
        System.out.println(o.getClass());
        System.out.println(o);

        o = 0;
        System.out.println(o.getClass());
        System.out.println(o);
    }
}
