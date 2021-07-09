package java16;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesFinallyJ16 {
    public void test(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            scanner.next();
        } finally {
            System.out.println("Hello");
        }
    }

    public void testFunc(File file) throws FileNotFoundException {
        try (Scanner scanner = create(file)) {
            scanner.next();
        } finally {
            System.out.println("Hello");
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
