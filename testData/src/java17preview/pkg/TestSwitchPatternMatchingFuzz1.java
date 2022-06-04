package pkg;

class TestSwitchPatternMatchingFuzz1 {

  public void test() {

    switch (new Object()) {
      default:
        throw new RuntimeException();
      case Double l:
        for (long none : new long[0]) {
          throw new RuntimeException();
        }
    }


    try {
      System.out.println("Hi");
      return;
    } catch (Exception vvv18) {
    } finally {
    }
    throw new RuntimeException();
  }
}
