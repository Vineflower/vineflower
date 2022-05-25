package pkg;

public class TestTryCatchNested {
  public void test() {
    float var1 = 20F;
    try {
      try {
        System.out.println(var1);
        return;
      } catch (Exception var7) {
      }
    } catch (Exception var10) {
      System.out.println(var1);
    }
  }
}
