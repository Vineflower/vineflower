package pkg;

import java.io.Serializable;

public class TestVarIndex2 {
  public void test() {
    {
      String a = "1";
      consume(a);
    }

    {
      CharSequence a = "1";
      consume(a);
    }

    {
      Serializable a = "1";
      consume(a);
    }

    {
      Object a = "1";
      consume(a);
    }
  }

  private void consume(String s) {

  }

  private void consume(CharSequence cs) {

  }

  private void consume(Serializable s) {

  }

  private void consume(Object o) {

  }
}
