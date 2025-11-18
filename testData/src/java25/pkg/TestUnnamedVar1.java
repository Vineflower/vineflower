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

  public void test2(Object o) {
    if (o instanceof Point _) {
      System.out.println(o);
    }
  }

  public void def() {
    int _ = 5;
    int x = 3;
    int _ = x;
    System.out.println(x);
  }

  public void patternSwitch(Object o) {
    switch (o) {
      case Integer _ -> System.out.println("1");
      default -> System.out.println("def");
    }
  }

  public void patternSwitch2(Object o) {
    switch (o) {
      case Integer _ -> System.out.println("1");
      case String _ -> System.out.println("2");
      default -> System.out.println("def");
    }
  }

  public void patternSwitch3(Object o) {
    switch (o) {
      case Integer _, String _ -> System.out.println("1");
      default -> System.out.println("def");
    }
  }

  public List<String> stream(List<String> in) {
    return in.stream().map(_ -> "val").toList();
  }

  public void trycatch(File file) {
    try {
      new Scanner(file);
    } catch (IOException _) {
      System.out.println("catch");
    }
  }

  public void trycatch2(File file) {
    try {
      new Scanner(file);
    } catch (IOException _) {
      System.out.println("catch");
    } catch (Exception _) {
      System.out.println("catch2");
    }
  }
}
