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

  public void test2() {
    Float f = 4.3f;

    other("object", f);
  }

  public void test3() {
    other("boxed", 4.3f);
  }

  private void consume(String s) {

  }

  private void consume(CharSequence cs) {

  }

  private void consume(Serializable s) {

  }

  private void consume(Object o) {

  }

  private void other(String s, Object o) {

  }

  private void other(String s, Object... o) {

  }
}
