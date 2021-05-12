package pkg;

public class TestInterfaceLambdaCast {
    public interface Func {
        int get();
    }

    private static int x = ((Func)() -> {
        System.out.println("Hi");
        return 1;
    }).get();

    private static void main() {
        System.out.println(x);
    }
}
