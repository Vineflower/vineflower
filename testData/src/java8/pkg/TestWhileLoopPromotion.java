package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestWhileLoopPromotion {
  public void test() {
    int i = 0;
    List<Integer> list = new ArrayList<>();
    while (i < 10) {
      list.add(i);
      i++;
    }
  }
}
