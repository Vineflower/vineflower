package pkg;

public class TestSynchronizeNull {
    public void test() {
        Object o = new Object();
        synchronized (o = null) {
            System.out.println("Hi");
        }
    }
}
