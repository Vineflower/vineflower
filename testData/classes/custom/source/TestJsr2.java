public class TestJsr2 {
    public static void main(String[] args) {
        try {
            System.out.println("Test");
        } finally {
            System.out.println("Jsr");
            return;
        }
    }

    public static void main2(String[] args) {
        try {
            try {
                System.out.println("Test");
            } finally {
                System.out.println("Jsr");
                return;
            }
        } finally {
            System.out.println("Jsr");
            return;
        }
    }

    public static void main3(String[] args) {
        try {
            try {
                System.out.println("Test");
                return;
            } finally {
                System.out.println("Jsr");
                return;
            }
        } finally {
            System.out.println("Jsr");
            return;
        }
    }
}