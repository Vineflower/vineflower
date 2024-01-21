package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesManyJ9 {
  public void test(File f) throws FileNotFoundException {
    try (
      Scanner s1 = new Scanner(f);
      Scanner s2 = new Scanner(f);
      Scanner s3 = new Scanner(f);
      Scanner s4 = new Scanner(f);
      Scanner s5 = new Scanner(f);
      ) {
      System.out.println(s1.nextLine());
      System.out.println(s2.nextLine());
      System.out.println(s3.nextLine());
      System.out.println(s4.nextLine());
      System.out.println(s5.nextLine());
    }
  }
}
