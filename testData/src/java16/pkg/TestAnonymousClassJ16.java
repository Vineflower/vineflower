package pkg;

public class TestAnonymousClassJ16 {
  public void testNamelessTypeVirtual() {
    var printer = new Object() {
      void println(String s) {
        System.out.println(s);
      }
    };
    printer.println("goodbye, world!");
  }

  public void testNamelessTypeVirtual2() {
    var printer = new TestAnonymousClassJ16() {
      void out(String s) {
        System.out.println(s);
      }
    };

    printer.out("goodbye, world!");
  }

  public void testNamelessTypeVirtual3() {
    TestAnonymousClassJ16 printer = new TestAnonymousClassJ16() {
      @Override
      public void testNamelessTypeVirtual() {
        System.out.println();
      }
    };

    printer.testNamelessTypeVirtual();
  }
}
