package pkg;

class TestAmbiguousCall {
  void m1(RuntimeException e, String s) { }
  void m1(IllegalArgumentException e, String s) { }

  void test() {
    IllegalArgumentException iae = new IllegalArgumentException();
    m1((RuntimeException)iae, "RE");
    m1(iae, "IAE");

    RuntimeException re = new IllegalArgumentException();
    m1(re, "RE");
    m1((IllegalArgumentException)re, "IAE");
  }

  void m2(int i) {}
  void m2(long l) {}
  void m2(String s) {}

  <T extends Comparable<T>> void test2(T value) {
    if (value instanceof Integer) {
      m2((Integer) value);
    } else {
      m2(value.toString());
    }
  }

  void test3(Object value) {
    if (value instanceof Integer) {
      m2((Integer) value);
    } else {
      m2(value.toString());
    }
  }

  int field;
  long field2;
  void test4() {
    m2(field++);
    m2((long)field++);

    m2((int)field2++);
    m2(field2++);
  }
}
