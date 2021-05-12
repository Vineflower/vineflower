package pkg;

public class TestInlineAssignments {
    private TestInlineAssignments(int i) {

    }

    public void testCall(int a) {
        int b;
        accept(b = a);
        System.out.println(b == a);
    }

    public void testConstructor(int a) {
        int b;
        new TestInlineAssignments(b = a);
        System.out.println(b == a);
    }

    public void testArray(int a) {
        int b;
        int[] array = new int[b = a];
        System.out.println(b == a);
    }

    public void accept(int i) {

    }
}
