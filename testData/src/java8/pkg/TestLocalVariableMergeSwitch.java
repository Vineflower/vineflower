package pkg;

public class TestLocalVariableMergeSwitch {
  public void test(String s, int j) {
    while (j > 0) {
      j--;

      int i = 0;

      switch (i) {
        case 1:
          i++;
          continue;
        case 2:
          i += s.length();
          continue;
        default:
          System.out.println("hi");
          continue;
      }
    }
  }
}
