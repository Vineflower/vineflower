package pkg;

import ext.TestMissingLambdaBodyExtra;

import java.util.function.Consumer;

public class TestMissingLambdaBody extends TestMissingLambdaBodyExtra {
  public void init() {
    this.visit(this::add);
  }

  public void visit(Consumer<Integer> var1) {
    var1.accept("Foo".hashCode());
    var1.accept("Bar".length());
  }
}
