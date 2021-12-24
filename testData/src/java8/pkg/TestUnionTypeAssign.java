package pkg;

import java.util.function.Supplier;

public interface TestUnionTypeAssign {
  default <E extends Enum<E> & TestUnionTypeAssign> void test(Supplier<E[]> supplier) {
    E[] array = supplier.get();
  }

  default <E extends Enum<E> & TestUnionTypeAssign> void test1(Supplier<E[]> supplier) {
    Enum[] array = supplier.get();
  }

  default <E extends Enum<E> & TestUnionTypeAssign> void test2(Supplier<E[]> supplier) {
    Enum<E>[] array = supplier.get();
  }

  default <E extends Enum<E> & TestUnionTypeAssign> void test3(Supplier<E[]> supplier) {
    TestUnionTypeAssign[] array = supplier.get();
  }

  default <E extends Enum<E> & TestUnionTypeAssign> void test4(Supplier<Enum[]> supplier) {
    E[] array = (E[])supplier.get();
  }

  default <E extends Enum<E> & TestUnionTypeAssign> void test5(Supplier<TestUnionTypeAssign[]> supplier) {
    E[] array = (E[])supplier.get();
  }
}
