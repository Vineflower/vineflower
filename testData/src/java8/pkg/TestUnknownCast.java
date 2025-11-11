package pkg;

public class TestUnknownCast {
  public void test() {
    boolean vvv1 = true, vvv2 = false;
    try {
      if (vvv2) {
        throw new RuntimeException();
      }
      vvv2 = vvv1;
    } finally {
      char vvv31;
    }
    vvv1 = vvv2;
    Object vvv34 = null;
    while (vvv34 != null) {
      vvv2 = vvv1;
      System.out.println(vvv34);
      vvv1 = vvv2;
    }
  }

  private void test1() {
    Object[] myObjects = new Object[]{};
    int[] myInts = new int[]{1, 2, 3};
    int i;
    int myObjectsLength = myObjects.length;
    for (i = 0; i < myInts.length; ++i) {
      int myInt = myInts[i];
      int[] myInts2 = myInts;
      int myInts2Length = myInts2.length;
      if (myInts2Length != myObjectsLength) {
        System.out.println(myInts2Length + " " + myObjectsLength);
      }
    }
  }
}
