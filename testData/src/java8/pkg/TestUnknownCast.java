package pkg;

public class TestUnknownCast {
  public void test() {
    boolean vvv1 = true, vvv2 = false;
    try {
      if (vvv2) {
        throw new RuntimeException();
      }
      vvv2 = vvv1;
    } finally {
      char vvv31;
    }
    vvv1 = vvv2;
    Object vvv34 = null;
    while (vvv34 != null) {
      vvv2 = vvv1;
      System.out.println(vvv34);
      vvv1 = vvv2;
    }
  }
}
