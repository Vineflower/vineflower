package pkg;

import ext.TestLibrary;

public class TestLibraryIsOnClasspath {
  void test() {
    System.out.println(TestLibrary.identity("Hello, World!"));
  }
}
