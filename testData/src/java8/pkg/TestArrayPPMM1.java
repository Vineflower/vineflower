package pkg;

public class TestArrayPPMM1 {
  public int[] test(int i) {
    return new int[] {++i};
  }

  public int[] test1(int i) {
    return new int[] {i, ++i};
  }

  public int[] test2(int i) {
    return new int[] {++i, i};
  }

  public int[] test3(int i) {
    return new int[] {++i, ++i};
  }
}
