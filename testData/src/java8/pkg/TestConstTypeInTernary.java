package pkg;

public class TestConstTypeInTernary {
  public void test(int num) {
    int newNum = num == 0 ? 0x8000 : num;
  }
}
