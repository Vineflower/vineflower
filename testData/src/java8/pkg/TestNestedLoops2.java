package pkg;

public class TestNestedLoops2 {
    public int foo(int i, int j) {
        while (true) {
            System.out.println("hi");
            try {
                while (i < j) {
                    i = j++ / i;
                }
            } catch (RuntimeException re) {
                i = 10;
                continue;
            }
            break;
        }
        return j;
    }
}
