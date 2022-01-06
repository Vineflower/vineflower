package pkg;

// Compiled with ecj 3.16.0
public class TestEclipseSwitchString {
    public int test(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "1":
                return 1;
            case "2":
                return 2;
        }

        return 0;
    }

    public int test1(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "1":
                return 1;
            case "2":
            case "3":
                return 2;
        }

        return 0;
    }

    public int test2(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "1":
                System.out.println("Hello");
            case "2":
            case "3":
                return 2;
        }

        return 0;
    }

    public int testHashcodeCollision(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "BB":
                return 1;
            case "Aa":
                return 2;
        }
        return 0;
    }

    public int testHashcodeCollision1(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "BB":
            case "Aa":
                return 2;
        }
        return 0;
    }

    public int testHashcodeCollision2(String s) {
        switch (s) {
            default:
                System.out.println("Test");
                break;
            case "BB":
                System.out.println("BB");
            case "Aa":
                return 2;
        }
        return 0;
    }
}
