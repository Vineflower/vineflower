package pkg;

public class TestSwitchAssign {
    public void test(int x) {
        int assign = 1;
        switch (x) {
            case 1:
            case 3:
            case 5:
                assign = 3;
                break;
            case 2:
            case 4:
            case 6:
                assign = 4;
                break;
        }

        System.out.println(assign);
    }
}
