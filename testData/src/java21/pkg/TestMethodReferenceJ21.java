package pkg;

import ext.RefsExt;

import java.util.function.Consumer;

public class TestMethodReferenceJ21 extends RefsExt {
  private void test() {
    consume(this::accept);
  }

  private void test2() {
    consume(this::accept2);
  }

  private void test3(TestMethodReferenceJ21 ref) {
    consume(ref::accept);
  }

  private void test4(TestMethodReferenceJ21 ref) {
    consume(ref::accept2);
  }

  private void test5(RefsExt ref) {
    consume(ref::accept2);
  }

  protected void consume(Consumer<String> c) {

  }
}
