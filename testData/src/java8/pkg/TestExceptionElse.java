package pkg;

import java.util.*;

public class TestExceptionElse {
  public void test(Deque<Number> numbers) {
    System.out.println("Test");

    while(!numbers.isEmpty()) {
      Number number = numbers.removeFirst();
      if (number instanceof Integer) {
        System.out.println(1);
      } else if (numbers.size() == 4 && number instanceof Long) {
        System.out.println(2);
      } else if (number instanceof Double) {
        System.out.println(3);
      } else if (numbers.size() == 3 && number instanceof Float) {
        System.out.println("here");
      } else {
        if (numbers.size() == 0) {
          System.out.println(4);
        }

        throw new RuntimeException("Not a valid number");
      }
    }
  }
}
