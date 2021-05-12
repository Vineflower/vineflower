package pkg;

public class TestSwitchOnStrings {
  public int testSOS1(String s) {
    switch (s) {
      case "xxx":
        return 2;
      case "yyy":
        return 1;
    }
    return 0;
  }

  public int testSOS2(String s) {
    switch (s) {
      case "xxx":
        return 2;
      case "yyy":
        return 1;
      case "BBAa":
        return 5;
      case "AaAa":
        return 4;
      case "AaBB":
        return 3;
    }
    return 0;
  }

  public int testSOS3(String s) {
    switch (s) {
      case "xxx":
        return 2;
      case "yyy":
        return 1;
      case "AaAa":
      case "AaBB":
      case "BBAa":
        return 3;
    }
    return 0;
  }
}
