package pkg;

public class TestInlineNoSuccessor {
  // Adapted from https://github.com/junit-team/junit4/blob/main/src/main/java/org/junit/runner/JUnitCommandLineParseResult.java#L53
  public String[] test(String... args) {
    for (int i = 0; i != args.length; ++i) {
      String arg = args[i];

      if (arg.isEmpty()) {
        return args;
      } else if (arg.startsWith("--")) {
        if (arg.startsWith("--a")) {
          System.out.println(0);

          if (arg.startsWith("--aa")) {
            ++i;

            if (i < args.length) {
              System.out.println(1);
            } else {
              break;
            }
          }

          System.out.println(2);
        }
      }
      else {
        return args;
      }
    }

    return new String[]{};
  }
}
