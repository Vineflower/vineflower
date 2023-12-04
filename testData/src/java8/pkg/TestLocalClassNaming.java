package pkg;

public abstract class TestLocalClassNaming {
  void foo() {
    class Local {
      int i = 4;

      Local() {
        i++;
      }
    }
    Local l = new Local();
  }
}
