package pkg;

public class TestLVTReassignment {
  public void test() {
    double one = 1;
    double shouldBeOne = one;
    one = 0;
    if (one > 1) {
    }

    blackhole(shouldBeOne);
  }

  void blackhole(double value) {

  }
}
