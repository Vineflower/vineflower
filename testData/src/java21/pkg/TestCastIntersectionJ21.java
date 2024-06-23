package pkg;

public class TestCastIntersectionJ21 {
  public void test1(I1 i1) {
    method((I1 & I2) i1);
  }

  public void test2(I2 i2) {
    method((I1 & I2) i2);
  }

  public void test3(I1 i1) {
    var i = (I1 & I2) i1;
    method(i);
  }

  public void test4(I2 i2) {
    var i = (I1 & I2) i2;
    method(i);
  }

  public void test5(I2 i2) {
    var i = (I1 & I2) i2;
    i.method();
  }

  public <I extends I1 & I2> void method(I i) {
  }

  private static class I1 {
  }

  private static interface I2 {
    public void method();
  }
}
