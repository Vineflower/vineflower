package pkg

class TestGroovyTryCatch {
  void test() {
    try {
      System.out.println("Hello");
    } catch (Exception e) {
      System.out.println("Exception");
    }
  }

  void test1() {
    try {
      System.out.println("Hello");
    } catch (Exception e) {
      System.out.println("Exception");
    } catch (Throwable t) {
      System.out.println("Throwable");
    }
  }
}
