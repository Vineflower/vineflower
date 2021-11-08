package pkg;

public class TestSwitchReturn {
    public int test(int x) {
        switch (x) {
            case 1:
            case 2:
            case 3:
                return 1;
            case 4:
            case 32:
            case 46:
                return 3;
            default:
                return 4;
        }
    }
}
