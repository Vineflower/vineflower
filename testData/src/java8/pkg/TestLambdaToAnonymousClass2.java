package pkg;

import java.util.List;
import java.util.function.*;

public class TestLambdaToAnonymousClass2 {
  public void test() {
    BiConsumer<List<String>, Integer> $ = (l, i) -> {
      Character[] a = l.stream()
        .map(st -> st.charAt(i))
        .toArray(Character[]::new);
    };
  }
}
