package pkg;

import java.util.ArrayList;

public class TestStaticInitOrder {
  // The static block assigns "d" before "c", and the constructor (run while initializing "c") reads "d".
  // Inlining both assignments as field initializers would reorder them to declaration order and read "d"
  // while it is still null.
  private static final TestStaticInitOrder c;
  private static final ArrayList<String> d;
  private final ArrayList<String> e = new ArrayList<>(d);

  static {
    d = new ArrayList<>();
    c = new TestStaticInitOrder();
  }
}
