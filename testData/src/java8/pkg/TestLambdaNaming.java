package pkg;

import java.util.List;
import java.util.function.BiConsumer;

public class TestLambdaNaming {
  public void test() {
    Runnable f = () -> System.out.println("test");
    BiConsumer<List<String>, Integer> g = (l, i) -> {
      Character[] a = l.stream()
        .map(st -> st.charAt(i))
        .toArray(Character[]::new);
    };
  }
}
