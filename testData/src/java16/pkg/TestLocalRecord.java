package pkg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TestLocalRecord {
  public void test(int i) {
    record Color(int red, int green, int blue) {}
    Color color = new Color(((i >> 16) & 0xFF) / 255, ((i >> 8) & 0xFF) / 255, (i & 0xFF) / 255);
    System.out.println(color);
  }

  public void test2() {
    record R() {}
    List<R> list = new ArrayList<>();
    list.add(new R());
  }

  public void test3() {
    record R() {
      static void nop() {}
    }
    Runnable nop = R::nop;
  }

  public void test4() {
    record R() {}
    Supplier<R> constr = () -> new R();
  }

  public Supplier<Supplier<Object>> test5() {
    record R() {}
    return () -> R::new;
  }

  public Supplier<Object> test6() {
    record R() {}
    return R::new;
  }
}
