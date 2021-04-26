package pkg;

public class TestAssignmentInDoWhile {
    public void testPP() {
        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x++) < 100);
        System.out.println("Hi");
    }

    public void testPE() {
        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x+=2) < 100);
        System.out.println("Hi");
    }

    public void testPEArray() {
        int[] array = new int[100];

        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x+=array[x]) < 100);
        System.out.println("Hi");
    }

    public void testPEArrayBitMask() {
        int[] array = new int[100];

        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x+=array[x] & 7) < 100);
        System.out.println("Hi");
    }

    public void testPEArrayAnd() {
        int[] array = new int[100];

        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x+=array[x]) < 100 && x > 10);
        System.out.println("Hi");
    }

    public void testPEArrayOr() {
        int[] array = new int[100];

        int x = 3;
        do {
            if (x < 50) {
                continue;
            }

            break;
        } while ((x+=array[x]) < 100 || x > 10);
        System.out.println("Hi");
    }
}
