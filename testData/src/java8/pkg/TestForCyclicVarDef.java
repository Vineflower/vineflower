package pkg;

public class TestForCyclicVarDef {
  public void test() {
    for (short var2 = ((short) 12338); var2 == ((short) -8396);) {
      return;
    }

    for (float var9 = 22.22F; var9 > 133.07F; var9 *= 29.43F) {
      var9 -= -15.01F;
      System.out.println("Hi");
    }
  }

  public void test1() {
    {
      short var2 = ((short) 12338);
    }

    for (float var9 = 22.22F; var9 > 133.07F; var9 *= 29.43F) {
      var9 -= -15.01F;
      System.out.println("Hi");
    }
  }

  public void testOk() {
    for (float var9 = 22.22F; var9 > 133.07F; var9 *= 29.43F) {
      var9 -= -15.01F;
      System.out.println("Hi");
    }
  }
}
