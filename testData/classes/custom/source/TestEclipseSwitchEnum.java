package pkg;

// Compiled with ecj 3.16.0
public enum TestEclipseSwitchEnum {
    A,
    B,
    C;

    public void test(TestEclipseSwitchEnum e) {
        switch (e) {
            case A:
                System.out.println("A");
                break;
            case B:
                System.out.println("B");
                break;
            case C:
                System.out.println("C");
                break;
        }
    }
}
