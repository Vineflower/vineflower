package pkg;

public class TestReturnTernaryChar {
    public int testChar(String input) {
        return "hello".equals(input) ? 34114 : 35311;
    }

    public int testShortChar(String input) {
        return "hello".equals(input) ? 32744 : 32321;
    }

    public int testByteChar(String input) {
        return "hello".equals(input) ? 34 : 21;
    }
}
