package pkg;

public class TestRecordPatterns7 {
  public void test(Object o) {
    if (o instanceof A(C(B(int i1, int i2), int i3), int i4, B(int i5, int i6))) {
      System.out.println(i1 + i2 + i3 + i4 + i5 + i6);
    }
  }

  public void test2(Object o) {
    System.out.println(switch (o) {
      case A(C(B(int i1, int i2), int i3), int i4, B(int i5, int i6)) -> i1 + i2 + i3 + i4 + i5 + i6;
      default -> 0;
    });
  }
  
  record A(C c, int i, B b2) {}

  record B(int i1, int i2) {}

  record C(B b, int i) {}
}
