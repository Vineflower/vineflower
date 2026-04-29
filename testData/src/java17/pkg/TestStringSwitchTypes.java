package pkg;

public class TestStringSwitchTypes {
  public int testSplit(String s) {
    int res = 0;
    switch (s) {
      case "a":
        res = 1;
        break;
      case "b":
        res = 2;
        break;
      case "c":
        res = 4;
        break;
    }
    return res;
  }

  public int testSplit2(String s) {
    return switch (s) {
      case "a" -> 1;
      case "b" -> 2;
      case "c" -> 4;
      default -> 0;
    };
  }

  public String testInlineSplit(String s) {
    String res = "";
    switch(s) {
      case "a":
        res = "a";
        break;
    }

    return res;
  }

  public int testMerged(String s) {
    switch (s) {
      default:
        System.out.println("Test");
        break;
      case "1":
        return 1;
      case "2":
        return 2;
    }

    return 0;
  }

  public int testSplitDup(String s) {
    switch (s) {
      case "a":
        return 1;
      case "b":
        return 1;
      default:
        System.out.println("Test");
        return 0;
    }
  }

  public int testMergedDup(String s) {
    switch (s) {
      default:
        System.out.println("Test");
        break;
      case "a":
        return 1;
      case "b":
        return 1;
    }

    return 0;
  }
}