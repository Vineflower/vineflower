package pkg;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class TestMethodReferenceJ25 {
  protected void test() {
    BiFunction<Object, Object, Object> c1 = getThis()::test1;
    BiFunction<Object, Object, Object> c2 = getThis()::test2;
    BiFunction<Object, Object, Object> c3 = getThis()::test3;
    BiFunction<Object, Object, Object> c4 = getThis()::test4;
    BiFunction<Object, Object, TestMethodReferenceJ25> c5 = TestMethodReferenceJ25::new;
    BiConsumer<Object, Object> c6 = getThis()::test5;
  }

  public TestMethodReferenceJ25(Object... o1) {

  }

  protected Object test1(Object... o1) {
    return o1;
  }

  protected Object test2(Object o1, Object... o2) {
    return o1;
  }

  protected Object test3(Object o1, Object o2, Object... o3) {
    return o1;
  }

  protected Object test4(Object o1, Object o2) {
    return o1;
  }

  protected void test5(Object... o1) {
  }

  public TestMethodReferenceJ25 getThis() {
    return this;
  }
}
