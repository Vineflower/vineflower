package pkg;

public class TestTryWithResourcesFakeTrigger {

  public void testTrigger1() {
    String a, b = "Hi!";

    try {
      try {
        System.out.println("Hi");
      } catch (Exception ignored) {
      }
      return;
    } catch (Exception ex) {
      try {
        a = b;
      } catch (Exception ignored) {
      }
    }
  }

  public void testTrigger2() {
    Object var1 = null;

    while (var1 == null) {
      try {
        System.out.println("Hi");
      } catch (Exception var21) {
        if (var1 != null) {
          break;
        }
        System.out.println(var1);
      }
    }
  }
}
