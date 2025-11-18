package pkg;

public class TestLoopMerging2 {
  public void test() {
    int[] array = new int[256];

    for (int i = 0; i < 256; i++) {
      array[i] = i;
    }

    for (int i = 0; i < 256; i++) {
      System.out.println(array[i]);
    }
  }

  public static void testVarRef(String stringOne) {
    while (true) {
      if (!stringOne.contains("a") && !stringOne.contains("b")) {
        return;
      }
      String stringTwo = "";
      int i = 1;
      if (i == -1) {
        stringTwo = "c";
      }
      stringOne = stringOne.replaceFirst(stringTwo, "");
    }
  }
}
