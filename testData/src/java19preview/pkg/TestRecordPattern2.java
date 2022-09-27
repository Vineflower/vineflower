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
}