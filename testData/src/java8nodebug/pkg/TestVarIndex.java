package pkg;

public class TestVarIndex {
  public void test() {
    {
      int a = 1;
      consume(a);
    }

    {
      short a = 1;
      consume(a);
    }

    {
      byte a = 1;
      consume(a);
    }
  }

  public void test2() {
    {
      int a = 1;
      consume(a);
    }

    short a = 1;
    consume(a);

    a = 1;
    consume((byte)a);
  }

  public void test3() {
    int a = 1;
    consume(a);

    a = 1;
    consume((short)a);

    a = 1;
    consume((byte)a);
  }

  public void test4() {
    byte a = 1;
    consume((int)a);

    a = 1;
    consume((short)a);

    a = 1;
    consume(a);
  }

  private void consume(int a) {

  }

  private void consume(short a) {

  }

  private void consume(byte a) {

  }
}
