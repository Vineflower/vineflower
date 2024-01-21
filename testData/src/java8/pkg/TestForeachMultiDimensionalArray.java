package pkg;

import java.lang.annotation.ElementType;

public class TestForeachMultiDimensionalArray {
  public void test() {
    int[][] vvv1 = new int[0][];
    for (int[] vvv2 : vvv1) {
      switch (1) {
        case 0: {
          try {
            ElementType vvv4 = ElementType.METHOD;
          } catch (Exception vvv8) {
            vvv1[0][0]++;
          } finally {
          }
        }
      }
    }
  }
}
