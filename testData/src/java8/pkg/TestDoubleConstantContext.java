package pkg;

public class TestDoubleConstantContext {
  private static final double DOUBLE_UNIT = 0x1.0p-53;

  public double nextDouble(long bits) {
    return bits * DOUBLE_UNIT;
  }

  public boolean less(float f) {
    return f < (double) 0.1F;
  }

  public boolean equal(long l) {
    return l == (double) 3.4E10F;
  }

  public Object boxed() {
    return (double) 5.999997E7F;
  }

  public String concatConstant(String s) {
    return s + (double) 1.0E-5F;
  }

  public double toDegrees(double radians) {
    return radians * (180.0F / (float) Math.PI);
  }

  public double scaled(double d) {
    return d * 0.1F;
  }
}
