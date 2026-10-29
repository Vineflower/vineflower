package pkg;

public class TestStringConcatWidening {
  public String floatToDouble(String s, float f) {
    return s + (double) f;
  }

  public String plain(String s, float f, double d) {
    return s + f + d;
  }
}
