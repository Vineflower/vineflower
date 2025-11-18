package pkg;

public class TestSwitchDefaultCaseReturn {
  public static String test(String s) {
    if (s.hashCode() == 0) {
      return switch (s) {
        case "", " " -> "";
        default -> s;
      };
    }

    int i = s.indexOf('a');
    boolean bl = false;

    while (true) {
      switch (s) {
        case "":
        case " ":
          return "";
        default:
          if (!bl && i != 0) {
            return "";
          } else if (bl) {
            return "";
          }

          if (s.indexOf('A' + i) == -1) {
            bl = true;
          }
      }
    }
  }
}
