package pkg;

public class TestRecordPattern1 {
  
  record Point(int a, int b) {}
  
  void test(Object o) {
    if(o instanceof Point(int a, int b)) {
      System.out.println(a + b);
    }
  }
}