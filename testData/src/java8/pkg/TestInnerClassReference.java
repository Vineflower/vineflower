package pkg;

import ext.SomeOuterClass;

public class TestInnerClassReference extends SomeOuterClass {
  public void callInner() {
    final SomeInner inner = new SomeInner();

    inner.greet();
  }
}
