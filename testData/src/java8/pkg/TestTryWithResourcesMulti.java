package pkg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestTryWithResourcesMulti {
    public void testMulti(File file) throws IOException {
        try (Scanner scanner = new Scanner(file); FileWriter writer = new FileWriter(file)) {
            scanner.next();
            writer.write("hello");
        }
    }

  public void testMulti2(File file) throws IOException {
      try (Scanner scanner = new Scanner(file)) {
          try (FileWriter writer = new FileWriter(file)) {
            scanner.next();
            writer.write("hello");
          }
      }
  }

  public void testMulti2Fake(File file) throws IOException {
    try (Scanner scanner = new Scanner(file)) {
      try (FileWriter writer = new FileWriter(file)) {
        scanner.next();
        writer.write("hello");
      } catch (Exception e) {
      }
    } catch (IOException e) {
    }
  }

  public void testMulti3(File file) throws IOException {
    try (Scanner scanner = new Scanner(file)) {
      try (FileWriter writer = new FileWriter(file)) {
        scanner.next();
        writer.write("hello");

        if (file == null) {
          System.out.println("??");
        }
      }
    }
  }
}
