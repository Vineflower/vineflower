package pkg;

public class TestLoopBreak4 {
  public void test2(int h, int i, boolean b) {
    for (int h1 = 0; h1 < h; h1++) {
      for (int j = 0; j < i; j++) {
        System.out.println(j);

        for (int k = 0; k < j; k++) {
          if (k == 2) {
            for (int l = 0; l < 2; l++) {
              System.out.println(2);

              if (b) {
                System.out.println(l);

                if (l == 1) {
                  for (int j1 = 0; j1 < h1; j1++) {
                    System.out.println(h1);
                    if (j1 == 1) {
                      break;
                    }
                    System.out.println(h1);
                  }
                  break;
                }
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
}
