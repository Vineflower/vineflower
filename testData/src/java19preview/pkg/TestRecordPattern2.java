package pkg;

public class TestRecordPattern2 {
  
  record Pair<A, B>(A a, B b) {}
  
  void test(Pair<?, ?> p) {
    if (p instanceof Pair<?,?>(String s, Long l)) {
      System.out.println("String-Long pair of \"" + s + "\" and " + l);
    } else if (p instanceof Pair<?,?>(Long l, Object o)) {
      System.out.println("Long-Object pair");
    } else {
      System.out.println("Other pair");
    }
  }
  
  void test2(Pair<?, ?> p) {
    if (p instanceof Pair<?,?>(String s, Long l)) {
      System.out.println("String-Long pair of \"" + s + "\" and " + l);
    } else if (p instanceof Pair<?,?>(Long l, Object o)) {
      System.out.println("Long-Object pair");
    } else {
      System.out.println("Other pair");
    }
    System.out.println("Unconditional");
  }
  
  Object test3(Pair<?, ?> p) {
    return switch(p) {
      case null -> -1;
      case Pair<?, ?>(Long l, Long r) v -> l + r;
      case Pair<?, ?>(Integer l, Integer r) v -> (l << r) * v.hashCode();
      case Pair<?, ?>(Object l, Void r) -> throw new IllegalArgumentException("how");
      case Pair<?, ?>(String l, String r)
        when l.length() > 3 && r.length() > 3 && l.length() + r.length() < 23
        -> l.length() + r.length();
      case Pair<?, ?>(Object l, Object r) p2 -> -2;
    };
  }
}