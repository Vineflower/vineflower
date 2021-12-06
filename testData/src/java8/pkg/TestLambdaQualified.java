package pkg;

import java.util.function.Supplier;

public class TestLambdaQualified {
  // Legal ONLY with the field qualification in the lambda!
  private static final Supplier<String> S = () -> TestLambdaQualified.STR;
  private static final String STR = make();

  // Outer -> inner
  private class Inner {
    private final Supplier<String> S = () -> TestLambdaQualified.STR;
  }

  // Inner -> outer
  private static class I1 {
    private static final Supplier<String> S = () -> TestLambdaQualified.I1.I2.STR2;
    private static final Supplier<String> S1 = () -> I1.I2.STR2;
    private static final Supplier<String> S2 = () -> I2.STR2;
    private static class I2 {
      private static final String STR2 = make();
    }
  }

  // Anonymous
  private static Runnable r = new Runnable() {
    private final Supplier<String> S_ = () -> TestLambdaQualified.STR;

    @Override
    public void run() {
      System.out.println(S_.get());
    }
  };

  // Prevent inlining
  private static String make() {
    return "str";
  }
}
