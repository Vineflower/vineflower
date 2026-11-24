package pkg;

public class TestPiDivision {
  public double div(double val) {
    return val / (180 / Math.PI);
  }

  public float mul(float f) {
    return f * (float) (Math.PI / 180.0);
  }

  public float mul2(float f) {
    return f * (180.0F / (float)Math.PI);
  }

  public float mul2_ok(float f) {
    return f * 180.0F / (float)Math.PI;
  }

  public double mul3(double val) {
    return val * (180 / Math.PI);
  }

  public double mul3_ok(double val) {
    return val * 180 / Math.PI;
  }

  public boolean isInf(double d) {
    return d == Double.POSITIVE_INFINITY;
  }
}
