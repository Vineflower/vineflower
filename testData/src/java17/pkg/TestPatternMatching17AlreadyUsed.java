package pkg;

public class TestPatternMatching17AlreadyUsed {
  void testFakeReuseVarSameNameOutOfScope(Object o) {
    {
      String s = "Hello";
      System.out.println(s);
    }

    if (o instanceof String) {
      String s = (String) o;
      System.out.println(s.length());
    }
  }

  void testRealReuseVarSameNameOutOfScope(Object o) {
    {
      String s = "Hello";
      System.out.println(s);
    }

    if (o instanceof String s) {
      System.out.println(s.length());
    }
  }

  void testFakeReuseVarDifferentNameOutOfScope(Object o) {
    {
      String sDifferent = "Hello";
      System.out.println(sDifferent);
    }

    if (o instanceof String) {
      String s = (String) o;
      System.out.println(s.length());
    }
  }

  void testRealReuseVarDifferentNameOutOfScope(Object o) {
    {
      String sDifferent = "Hello";
      System.out.println(sDifferent);
    }

    if (o instanceof String s) {
      System.out.println(s.length());
    }
  }

  void testFakeReuseVarNoPhi(Object o) {
    String s = "Hello";

    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
  }

  void testFakeReuseVarNoPhiStillUsed(Object o) {
    String s = "Hello";

    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    } else {
      System.out.println(s.length());
    }
  }

  void testFakeReuseVarPhi(Object o) {
    String s = "Hello";

    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
  }

  void testFakeReuseLoopNoPhi(Object[] o) {
    String s = "Hello";

    for (int i = 0; i < 10; i++) {
      if (o[i] instanceof String) {
        s = (String) o[i];
        System.out.println(s.length());
      }
    }
  }

  void testFakeReuseLoopPhi(Object[] o) {
    String s = "Hello";

    for (int i = 0; i < 10; i++) {
      if (o[i] instanceof String) {
        s = (String) o[i];
      }
    }
    System.out.println(s.length());
  }

  void testFakeDoubleReuseNoPhi(Object o) {
    String s = "Hello";
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
  }

  void testFakeQuadrupleReuseNoPhi(Object o) {
    String s = "Hello";
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
    if (o instanceof String) {
      s = (String) o;
      System.out.println(s.length());
    }
  }

  void testFakeDoubleReusePhi(Object o) {
    String s = "Hello";
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
  }

  void testFakeQuadrupleReusePhi(Object o) {
    String s = "Hello";
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
    if (o instanceof String) {
      s = (String) o;
    }
    System.out.println(s.length());
  }
}
