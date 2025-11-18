package pkg;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class TestGenericArrays<T extends Number> {
  public final T[] arr;
  public final T[][] multi;

  public TestGenericArrays(int i) {
    arr = (T[]) new Number[i];
    multi = (T[][]) new Number[i][];
  }

  public static <K extends Enum<K>, V> Map<K, V> makeEnumMap(Class<K> enumClass, Function<K, V> valueGetter) {
    EnumMap<K, V> map = new EnumMap<>(enumClass);

    for (K _enum : enumClass.getEnumConstants()) {
      map.put(_enum, valueGetter.apply(_enum));
    }

    return map;
  }
}
