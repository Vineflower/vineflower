package pkg;

import ext.ExampleAnnotation;

public record TestTextTokens2<T>(String name, @ExampleAnnotation T value, int index, Object... args) {
  public void foo() {
    System.out.println(name + ": " + value);
  }
}
