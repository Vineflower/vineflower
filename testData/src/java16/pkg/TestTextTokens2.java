package pkg;

import ext.ExampleAnnotation;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record TestTextTokens2<T>(String name, @ExampleAnnotation T value, int index, Object... args) {
  public void foo() {
    System.out.println(name + ": " + value);
  }

  public void bar(Supplier<Optional<? extends BiConsumer<T, String>>> r) {
    String s = "Hello world";
    r.get().ifPresent(c -> c.accept(value, s));
  }

  public Function<String, String> baz() {
    return s -> s + " " + this.name;
  }
}
