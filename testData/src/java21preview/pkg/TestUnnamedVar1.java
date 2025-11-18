package pkg;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class TestUnnamedVar1 {
  record Point(int x, Object y) {}

  public void test(Object o) {
    if (o instanceof Point(int x, _)) {
      System.out.println(x);
    }
  }

  public void test1(Object o) {
    if (o instanceof Point(int x, Integer _)) {
      System.out.println(x);
    }
  }

  public List<String> stream(List<String> in) {
    return in.stream().map(_ -> "val").toList();
  }

  public void trycatch(File file) {
    try {
      new Scanner(file);
    } catch (IOException _) {

    }
  }
}
