package pkg;

public class TestStringLiteral {
    public void testSplit() {
        String[] array = "!~".split("!");
        System.out.println(array.length);
    }

    public void testEquals() {
        if ("".equals("")) {
            System.out.println("Hi");
        }
    }

    public void testReplace() {
        System.out.println("Hello".replace("l", "c"));
    }
}
