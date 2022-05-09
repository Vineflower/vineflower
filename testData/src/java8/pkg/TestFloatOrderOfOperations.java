package pkg;

public class TestFloatOrderOfOperations {
  public float test(float a, float b, float c) {
    return a + (b + c);
  }

  public float testReference(float a, float b, float c) {
    return a + b + c;
  }
}
