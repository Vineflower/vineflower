package pkg;

import java.util.*;

public class TestExceptionElse {
  public void test(Deque<Number> numbers) {
    System.out.println("Test");

    while(!numbers.isEmpty()) {
      Number number = numbers.removeFirst();
      if (number instanceof Integer) {
        System.out.println(1);
      } else if (numbers.size() == 3 && number instanceof Float) {
        System.out.println("here");
      } else {
        throw new RuntimeException("Not a valid number");
      }
    }
  }
}
