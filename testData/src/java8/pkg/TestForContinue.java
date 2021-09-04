package pkg;

public class TestForContinue {
  public void test(int j) {
    for (int i = 0; i < j; i++) {
      if (i == 4) {
        continue;
      }

      System.out.println(i);
    }
  }
}
