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
}
