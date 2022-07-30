package pkg;

public class TestLoopBreak3 {
  public void test(int i, boolean b) {
    for (int j = 0; j < i; j++) {
      System.out.println(j);

      for (int k = 0; k < j; k++) {
        if (k == 2) {
          for (int l = 0; l < 2; l++) {
            System.out.println(2);

            if (b) {
              System.out.println(l);
            } else {
              break;
            }
          }

          break;
        }
      }
    }
  }
}
