package pkg;

public class TestInnerClasses3J21 {
  private int i = 0;

  private class Inner {
    private int j;
    Inner() {
      this(0);
    }

    Inner(int i) {
      this.j = i;
    }

    private void setI() {
      TestInnerClasses3J21.this.i = j;
    }
  }
}
