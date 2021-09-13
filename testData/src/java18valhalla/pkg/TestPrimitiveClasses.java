package pkg;

import java.util.List;

public class TestPrimitiveClasses {
  static primitive class VT {
    int value;

    public VT(int value) {
      this.value = value;
    }

    public VT(long value) {
      this((int) value);
      System.out.println(new VT(73));
    }

    public VT(boolean value) {
      this(value ? 1 : 0);
    }

    VT negate() {
      return negative(this);
    }

    static VT negative(VT x) {
      return new VT(x.value);
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

  static VT test1() {
    return new VT(42);
  }

  static VT test2() {
    return VT.default;
  }

  static primitive record PrimitiveRecord(float value) {}
}
