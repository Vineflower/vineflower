package java16;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesCatchFinallyJ16 {
    public void test(File file) {
        try (Scanner scanner = new Scanner(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Hello");
        }
    }

    public void testFunc(File file) {
        try (Scanner scanner = create(file)) {
            scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Hello");
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
