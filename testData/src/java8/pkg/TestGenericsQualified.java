package pkg;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

// Science!
public class TestGenericsQualified {
  public Comparator<String> field = Comparator.<String, Integer>comparing(s -> s.length()).thenComparing(i -> i.toString());
  public CompletableFuture<String> field2 = CompletableFuture.supplyAsync(() -> "").thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "2"));
  public Optional<String> field3 = Optional.of("").map(s -> s + "3");
  public Stream<String> field4 = Stream.of("1", "2")
    .sorted(Comparator.<String, Integer>comparing(s -> s.length()).thenComparing(i -> i.toString()));
  public Comparator<String> field5 = Comparator.comparing(String::length).thenComparing(i -> i.toString());
  public Comparator<TestGenericsQualified> field6 = Comparator.<TestGenericsQualified, Integer>comparing(TestGenericsQualified::get).reversed();

  public int get() {
    return 0;
  }

  public int get(int i) {
    return i;
  }

  public Comparator<String> method() {
    return Comparator.<String, Integer>comparing(s -> s.length()).thenComparing(i -> i.toString());
  }
}
