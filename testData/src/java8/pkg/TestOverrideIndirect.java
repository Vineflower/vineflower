package pkg;

import java.util.function.Function;

public class TestOverrideIndirect {
  public interface A {

  }

  public interface B {

  }

  public interface Magic extends Function<A, B> {
    String name();
  }

  public class Sparkles implements Magic {
    @Override
    public String name() {
      return "Sparkles";
    }

    @Override
    public B apply(A a) {
      return null;
    }
  }
}
