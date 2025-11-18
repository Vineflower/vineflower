package pkg;

public class TestInnerClasses2J21 {
  private void test() {
    new Inner().new Inner2(true, true);
  }

  private class Inner {
    private class Inner2 {
      private Inner2(boolean nonFinal, final boolean finalB) {
        TestInnerClasses2J21.this.test();
      }
    }
  }
}
