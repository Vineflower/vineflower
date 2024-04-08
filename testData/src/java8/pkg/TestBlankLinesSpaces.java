package pkg;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class TestBlankLinesSpaces {
  private static final Map<LongName, BiFunction<LongName2, LongName2, LongName3>> SHAPE_BUILDERS = ImmutableMap.<LongName, BiFunction<LongName2, LongName2, LongName3>>builder()
    .put(LongName.A, (a, b) -> null)
    .put(LongName.B, (a, b) -> null)
    .put(LongName.C, (a, b) -> null)
    .build();

  enum LongName {
    A,
    B,
    C
  }
  interface LongName2 {}
  interface LongName3 {}
  static final class ImmutableMap {
    static <K, V> Builder<K, V> builder() {
      return new Builder<>();
    }

    static final class Builder<K, V> {
      Builder<K, V> put(K key, V value) {
        return this;
      }

      Map<K, V> build() {
        return new HashMap<>();
      }
    }
  }
}
