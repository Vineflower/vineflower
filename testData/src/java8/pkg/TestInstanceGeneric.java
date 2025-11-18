package pkg;

public class TestInstanceGeneric<N extends Number> {
  public void accept(N num) {

  }

  public static TestInstanceGeneric<?> get() {
    return null;
  }

  public static void test(Long l) {
    TestInstanceGeneric<?> val = get();

    ((TestInstanceGeneric<Long>)val).accept(l);
  }
}
