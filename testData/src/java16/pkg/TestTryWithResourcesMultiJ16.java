package pkg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesMultiJ16 {
    public void testMulti(File file) throws IOException {
        try (Scanner scanner = new Scanner(file); FileWriter writer = new FileWriter(file)) {
            scanner.next();
            writer.write("hello");
        }
    }
}
