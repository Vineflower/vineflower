package pkg;

public class TestAnonymousObject {
    public void test() {
        Object o = new Object() {
            {
                System.out.println("Hi");
            }
        };
    }
}
