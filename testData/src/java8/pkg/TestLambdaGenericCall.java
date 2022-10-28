package pkg;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TestLambdaGenericCall {
  public void test(List<String> list, Map<String, Number[]> map) {
    list.forEach(s -> {
      Number[] numbers = map.get(s);

      System.out.println(numbers[0]);
    });
  }

  public void test2(List<String> list, Map<String, Number[]> map) {
    list.forEach(s -> {
      System.out.println(map.get(s)[0]);
    });
  }

  public Map<String, Number[]> map;

  public void test(List<String> list) {
    list.forEach(s -> {
      Number[] numbers = map.get(s);

      System.out.println(numbers[0]);
    });
  }
}
