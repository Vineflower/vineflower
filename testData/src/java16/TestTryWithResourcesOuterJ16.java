package java16;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesOuterJ16 {
    public void test(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        try (scanner) {
            scanner.next();
        }
    }

    public void testFunc(File file) throws FileNotFoundException {
        Scanner scanner = create(file);

        try (scanner) {
            scanner.next();
        }
    }

    public void testMulti(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        FileWriter writer = new FileWriter(file);

        try (scanner; writer) {
            scanner.next();
            writer.write("hello");
        }
    }

    public void testNested1(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        FileWriter writer = new FileWriter(file);

        try (scanner) {
            scanner.next();

            try (writer) {
                writer.write("hello");
            }
        }
    }

    public void testNested2(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        FileWriter writer = new FileWriter(file);

        try (scanner) {
            try (writer) {
                scanner.next();
                writer.write("hello");
            }
        }
    }

    public void testSame1(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        try (scanner) {
            scanner.next();

            try (scanner) {
                scanner.next();
            }
        }
    }

    public void testSame2(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        try (scanner) {
            try (scanner) {
                scanner.next();
            }
        }
    }

    public void testSame3(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        try (scanner) {
            try (scanner) {
                scanner.next();
            }

            scanner.next();
        }
    }

    public void testSame4(File file) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            try (scanner) {
                scanner.next();
            }
        }
    }

    private Scanner create(File file) throws FileNotFoundException {
        return new Scanner(file);
    }
}
