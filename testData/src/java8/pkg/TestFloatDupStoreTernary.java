package pkg;

public class TestFloatDupStoreTernary {
  public static float min4(float a, float b, float c, float d) {
    float t;
    return d < (t = c < (t = a < b ? a : b) ? c : t) ? d : t;
  }

  public static float max4(float a, float b, float c, float d) {
    float t;
    return d > (t = c > (t = a > b ? a : b) ? c : t) ? d : t;
  }
}