package pkg;

import java.util.List;

public class TestRefDefaultPrimitiveClasses {
  static primitive class VT.val {
    int value;

    public VT(int value) {
      this.value = value;
    }
  }

  static void takesRef(VT.ref r) {
    System.out.println(r.value);
  }

  static void takesVal(VT.val v) {
    System.out.println(v.value);
  }

  static void takesDefault(VT t) {
    System.out.println(t.value);
  }
}
