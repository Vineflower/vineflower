package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesCatchJ16 {
    public void test(File file) {
        try (Scanner scanner = new Scanner(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void testFunc(File file) {
        try (Scanner scanner = create(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
