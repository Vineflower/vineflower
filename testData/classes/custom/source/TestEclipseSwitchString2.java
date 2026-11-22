package pkg;

// Compiled with ecj 3.45.0
public class TestEclipseSwitchString2 {
  String field = "";

  public static class Inner {
    public static String get() {
      return "";
    }

    public String getVirtual() {
      return "";
    }
  }

  public String testInvokeStatic() {
    String s = "a";
    switch (Inner.get()) {
      case "1":
        s = "b";
        break;
      case "2":
        s = "c";
        break;
      case "3":
        s = "d";
        break;
    }

    return s;
  }

  public String testInvokeVirtual() {
    String s = "a";
    switch (new Inner().getVirtual()) {
      case "1":
        s = "b";
        break;
      case "2":
        s = "c";
        break;
      case "3":
        s = "d";
        break;
    }

    return s;
  }
}
