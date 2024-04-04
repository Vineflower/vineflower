package pkg;

import java.lang.reflect.Array;
import java.util.stream.IntStream;

public class TestArrayGenerics {
  private static <T> T[] myMethod(Object[] myObjects, Class<T> clazz) {
    return IntStream.range(0, myObjects.length)
      .mapToObj(i -> myObjects[i])
      .toArray(size -> (T[]) Array.newInstance(clazz, size));
  }
}
