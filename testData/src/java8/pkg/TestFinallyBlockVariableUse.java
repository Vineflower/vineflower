package pkg;

public class TestFinallyBlockVariableUse {
  public int test(String s, int i, int j) {
    System.out.println("a");
    try {
      System.out.println("b");
      try {
        i = Integer.parseInt(s) - j;

        return i;

      } catch (NumberFormatException e) {
        i = j;
        throw e;
      }
    } finally {
      int id = i - j;
      if (id > 0) {

        accept(new Object[]{id, s, i, j});
      }
    }
  }

  private boolean condition(int i) {
    return i-- > 0;
  }

  private void accept(Object[] args) {

  }
}
