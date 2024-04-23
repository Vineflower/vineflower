package pkg;

import java.util.Random;
import java.util.function.Predicate;

public class TestSwitchSimpleMultiLine {
  public static <T> Predicate<T> predicates() {
    int i = new Random().nextInt();
    return switch (i) {
      case 0 -> object -> true;
      case 1 -> object -> false;
      default -> object -> {
        final boolean r1 = new Random().nextBoolean();
        final boolean r2 = new Random().nextBoolean();
        if (r1 && r2) {
          return true;
        }
        return new Random().nextBoolean();
      };
    };
  }
}
