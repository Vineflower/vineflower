package pkg;

public class TestJADNaming {
    public void Func() {
        int a = 1000, b = 2000, c = 3000, d = 4000;
        if (a == b || c == d);
        boolean xa = true, xb = false, xc = true, xd = false;
        if (xa == xb || xc == xd);
    }

    public void types() {
      short a = 0;
      short aa = 0;
      byte b = 0;
      byte bb = 0;
      char c = 'c';
      char cc = 'c';
      double d = 0.5;
      double dd = 0.5;
      float f = 0.5f;
      float ff = 0.5f;

      int e = 1;
      int ee = 1;
      long g = 1;
      long gg = 1;
      int ge = 1;
      int gf = 1;
      long gh = 1;
    }

    public void objects() {
      Class a = null;
      Class b = null;

      Enum e = null;
      Enum ee = null;

      Package p = null;
      Package pp = null;
    }
}