package pkg;

import ext.ExampleAnnotation;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record TestTextTokens2<T>(String name, @ExampleAnnotation T value, int index, Object... args) {
  public void foo() {
    System.out.println(name + ": " + value);
  }

  public void bar(Supplier<Optional<? extends Consumer<T>>> r) {
    r.get().ifPresent(c -> c.accept(value));
  }
}
