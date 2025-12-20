package pkg;

import ext.RefsExt;

import java.util.function.Consumer;

public class TestMethodReferenceJ25 extends RefsExt {
  private void test() {
    consume(this::accept);
  }

  private void test_() {
    consume(s -> this.accept(s));
  }

  private void test2() {
    consume(this::accept2);
  }

  private void test3(TestMethodReferenceJ25 ref) {
    consume(ref::accept);
  }

  private void test3_(TestMethodReferenceJ25 ref) {
    consume(s -> ref.accept(s));
  }

  private void test4(TestMethodReferenceJ25 ref) {
    consume(ref::accept2);
  }

  private void test5(RefsExt ref) {
    consume(ref::accept2);
  }

  private void test6() {
    consume(ref()::accept);
  }

  private void test6_() {
    consume(s -> ref().accept(s));
  }

  private void test7() {
    consume(ref()::accept2);
  }

  public TestMethodReferenceJ25 ref() {
    return this;
  }

  protected void consume(Consumer<String> c) {

  }
}
