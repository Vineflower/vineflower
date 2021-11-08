package pkg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesNestedJ16 {
    public void testNested(File file) throws IOException {
        try (Scanner scanner = new Scanner(file)) {
            try (Scanner scanner1 = new Scanner(file)) {
                try (Scanner scanner2 = new Scanner(file)) {
                    try (Scanner scanner3 = new Scanner(file)) {
                        scanner.next();
                    }
                }
            }
        }
    }

    public void testNestedFinally(File file) throws IOException {
        try (Scanner scanner = new Scanner(file)) {
            try (Scanner scanner1 = new Scanner(file)) {
                try (Scanner scanner2 = new Scanner(file)) {
                    try (Scanner scanner3 = new Scanner(file)) {
                        scanner.next();
                    } finally {
                        System.out.println(4);
                    }
                } finally {
                    System.out.println(3);
                }
            } finally {
                System.out.println(2);
            }
        } finally {
            System.out.println(1);
        }
    }
}
