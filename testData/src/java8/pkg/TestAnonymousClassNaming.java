package pkg;

public class TestAnonymousClassNaming {
  public void run(int i, String s) throws Exception {
    Object o = new Object() {};
    if (i < 0) {
      throw new Exception() {
        @Override
        public String getMessage() {
          return s;
        }
      };
    }
  }

  private static class InnerClass {
    Object o = new Object() {
    };
  }
}
