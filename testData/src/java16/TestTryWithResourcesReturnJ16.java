package java16;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesReturnJ16 {
    public Scanner test(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            return scanner;
        }
    }

    public Scanner testFunc(File file) throws FileNotFoundException {
        try (Scanner scanner = create(file)) {
            return scanner;
        }
    }

    public Scanner testFinally(File file) {
        try (Scanner scanner = new Scanner(file)) {
            return scanner;
        } finally {
            return null;
        }
    }

    public Scanner testFinallyNested(File file) {
        try (Scanner scanner = new Scanner(file)) {
            try (Scanner scanner2 = new Scanner(file)) {
                return scanner2;
            } finally {
                return scanner;
            }
        } finally {
            return null;
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
