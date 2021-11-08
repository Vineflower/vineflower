package pkg;

public class TestLocalEnum {
  public void test(int i) {
    enum Type {
      VALID,
      INVALID
    }

    Type type = i == 0 ? Type.VALID : Type.INVALID;
  }
}
