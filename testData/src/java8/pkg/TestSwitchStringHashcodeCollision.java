package pkg;

public class TestSwitchStringHashcodeCollision {
    public int test(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "BB":
                return 1;
            case "Aa":
            case "FRED":
                return 2;
        }
        return 0;
    }
}
