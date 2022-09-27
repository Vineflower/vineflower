package pkg;

public class TestRecordPattern1 {
  
  record Point(int a, int b) {}
  
  void test(Object o) {
    if (o instanceof Point(int a, int b)) {
      System.out.println(a + b);
    }
  }
  
  void test2(Object o) {
    switch(o) {
      case Point(var a, var b) -> System.out.println(a + b);
      case null -> System.out.println("null");
      case Object obj -> System.out.println("everything else");
    }
  }
  
  int test3(Object o) {
    return switch(o) {
      case null -> -1;
      case Point(var a, var b)
        when a > 0 && b > 0
        -> a + b;
      case Point p -> throw new IllegalArgumentException("Negative point not allowed");
      case Object object -> throw new IllegalArgumentException("Only points or null allowed");
    };
  }
}