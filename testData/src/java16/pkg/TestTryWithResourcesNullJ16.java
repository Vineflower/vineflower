package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesNullJ16 {
    public void test(File file) throws FileNotFoundException {
        try (Scanner scanner = null) {
            scanner.next();
        }
    }

    public void testNested(File file) {
        try (Scanner scanner = null) {
            scanner.next();

            try (Scanner scanner2 = null) {
                scanner2.next();
            }
        }
    }

    public void testMulti(File file) {
        try (Scanner scanner = null; Scanner scanner2 = null) {
            scanner.next();
            scanner2.next();
        }
    }
}
