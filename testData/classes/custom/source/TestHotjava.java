public class TestHotjava {
    public void test() {
        System.out.println("Hello!");
    }

    public void testJsr() {
        try {
            System.out.println("Try");
        } finally {
            System.out.println("Jsr");
        }
    }

    public void testMonitor1() {
        synchronize (this) {
            System.out.println("Synchronized");
        }
    }

    public void testMonitor2(Object o) {
        synchronize (o) {
            System.out.println("Synchronized");
        }
    }

    public void testMonitor3() {
        synchronize (this) {
            try {
                System.out.println("Try");
            } finally {
                System.out.println("Jsr");
            }
        }
    }
}