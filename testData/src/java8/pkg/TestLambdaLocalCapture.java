package pkg;

public class TestLambdaLocalCapture {
  public void test(int x, int z) {
    System.out.println(1);

    for (int x1 = 0; x1 < 100; x1++) {
      for (int z1 = 0; z1 < 100; z1++) {
        int finalX = x1;
        int finalZ = z1;

        accept(() -> {
          System.out.println((x << 8) + finalX + " " + (z << 8) + finalZ);

          System.out.println(1);
          System.out.println(2);
        });
      }
    }

    System.out.println(1);
  }

  private static void accept(Runnable r) {

  }
}
