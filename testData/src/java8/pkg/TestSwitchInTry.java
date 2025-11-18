package pkg;

import java.util.Arrays;
import java.util.List;

public class TestSwitchInTry {
  /**
   * ISSUE 349: Infinite loop in SFormsConstructor->splitVariables
   * Submitted by @mvisat
   */

  public static List<String> method(String[] args) {
    String ret = "";
    for (String arg : args) {
      try {
        switch (arg) {
          case "a":
            ret = "a";
        }
        break;
      } catch (Exception ex) {
      }
    }
    return Arrays.asList(ret);
  }
}
