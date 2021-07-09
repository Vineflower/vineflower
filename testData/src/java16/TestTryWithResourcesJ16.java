package java16;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesJ16 {
    public void test(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            scanner.next();
        }
    }

    public void testFunc(File file) throws FileNotFoundException {
        try (Scanner scanner = create(file)) {
            scanner.next();
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
