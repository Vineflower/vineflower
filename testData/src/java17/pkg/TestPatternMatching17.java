package pkg;

public class TestPatternMatching17 {
    public void testSimple(Object obj) {
        if (obj instanceof String str) {
            System.out.println(str.length());
        }
    }

    public void testInverted(Object obj) {
        if (!(obj instanceof String str)) {
            System.out.println("Oh no");
        }
    }

    public void testCompound(Object obj) {
        if (obj instanceof String str && str.contains("hi")) {
            System.out.println(str.length());
        }
    }


    public void testSimpleLoop(Object obj) {
        while (obj instanceof String str) {
            System.out.println(str.length());
            obj = str.intern();
        }
    }

  public void testSimpleLoopUnused(Object obj) {
    while (obj instanceof String str) {
      obj = obj.hashCode() + "";
    }
  }

    public void testInvertedLoop(Object obj) {
        while (!(obj instanceof String str)) {
            System.out.println("Oh no");
            obj = obj.toString();
        }
        System.out.println(str.hashCode());
    }

  public void testInvertedLoopUnused(Object obj) {
    while (!(obj instanceof String str)) {
      System.out.println("Oh no");
      obj = obj.toString();
    }
  }

    public void testCompoundLoop(Object obj) {
        while (obj instanceof String str && str.contains("hi")) {
            obj = str.substring(1);
        }
    }

    public boolean testReturn(Object obj) {
        return obj instanceof String s && s.length() > 5;
    }

    public int testReturnTernary(Object obj) {
        return obj instanceof String s ? s.length() : 0;
    }

    public int testReturnTernaryComplex(Object obj) {
        return obj instanceof String s && s.length() > 5 || obj instanceof Integer ? 4 : 1;
    }

    public void testLoop(Object obj) {
        while (obj instanceof String s && s.length() > 10) {
            s = s.substring(1);
            obj = s.substring(1);

            System.out.println(s);
        }
    }

  public void testSimpleReturn(Object obj) {
    if (obj instanceof String str) {
      if (str.length() > 5) {
        return;
      }
    }

    System.out.println("test");
  }

  public void testMessyLVT(Object obj) {
        {
            String a = "a";
            String b = "b";
            String c = "c";
            String d = "d";
            String e = "e";
            String f = "f";
            String g = "g";
            String h = "h";
            String i = "i";
        }
        if (obj instanceof String str) {
            System.out.println(str.length());
        }
    }

    private Object object;
    private AutoCloseable o1;
    private int x;
    private int y;

    public void testEmptyHead() throws Throwable {
      if (object instanceof String) {
        if (this.o1 != null) {
          this.o1.close();
        }

        this.y = (this.x + 1) % 5;
        if (this.y == 0) {
          System.out.println(0);
        } else {
          System.out.println(1);
        }
      }
    }

  public void reassign(Object o) {
    String s = "hello";
    for (int i = 0; i < 10; i++) {
      if (o instanceof Number) {
        s = "goodbye";
      } else if (o instanceof String) {
        s = (String) o;
      }
    }

    System.out.println(s);
  }

  public void reassignCopy(Object o) {
    String s = "hello";
    for (int i = 0; i < 10; i++) {
      if (o instanceof Number) {
        s = "goodbye";
      } else if (o instanceof String s2) {
        s = s2;
      }
    }

    System.out.println(s);
  }

  public void reassignField(Object o, String s) {
    for (int i = 0; i < 10; i++) {
      if (o instanceof Number) {
        s = "goodbye";
      } else if (o instanceof String) {
        s = (String) o;
      }
    }

    System.out.println(s);
  }

  public void reassignNoUse(Object o) {
    String s = "hello";
    for (int i = 0; i < 10; i++) {
      if (o instanceof Number) {
        s = "goodbye";
      } else if (o instanceof String) {
        s = (String) o;
      }
    }
  }

  public String multiCombo(Object o, String s) {
      if (o instanceof String s2 && !s.isEmpty()) {
        return s2 + s;
      }
      return s;
  }
}
