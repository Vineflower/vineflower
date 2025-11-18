package pkg;

import ext.SomeOuterClass;

import java.lang.Exception;
import java.lang.Override;
import java.lang.Runnable;
import java.util.Iterator;

public abstract class TestAnonymousClass {
  void foo(int i)
    throws Exception {
    if (i > 0) {
      I r = new I() {
        public void foo() throws Exception {
          int a = 5;
          int b = 5;
        }
      };
      r.foo();
    }
    else {
      final int x =5;
      System.out.println(x);
    }
  }

  void bar() {
    new Object() {
      public void foo(String s) {
        System.out.println(s);
      }
    }.foo("Hello world");
  }

  public static final Runnable R3 = new Runnable() {
    @Override
    public void run() {
      int a =5;
      int b =5;
    }
  };


  void boo() {
    int a =5;
  }

  void zoo() {
    int a =5;
  }

  public static final Runnable R = new Runnable() {
    @Override
    public void run() {
      int a =5;
      int b =5;
    }
  };

  public static final Runnable R1 = new Runnable() {
    @Override
    public void run() {
      int a =5;
      int b =5;
    }
  };

  interface I {
    void foo() throws Exception;
  }

  private static class Inner {
    private static Runnable R_I = new Runnable() {
      @Override
      public void run() {
        int a =5;
        int b =5;
      }
    };
  }

  private final InnerRecursive y = new InnerRecursive(new InnerRecursive(null) {
    @Override
    void foo() {
      int a =5;
      int b =5;
      int g =5;
    }
  }) {
    int v =5;
    int t =5;
    int j =5;
    int o =5;
  };


  private final InnerRecursive x = new InnerRecursive(new InnerRecursive(null) {
    @Override
    void foo() {
      int a =5;
      int b =5;
      int g =5;
    }
  }) {
    int v =5;
    int t =5;
    int j =5;
    int o =5;
  };

  static class InnerRecursive {
    InnerRecursive r;

    public InnerRecursive(InnerRecursive r) {
      this.r = r;
    }

    void foo() {

    }
  }

  public static Iterable<Integer> innerInAnon() {
    return new Iterable<Integer>() {
      public int field = 1491401;

      class Inner implements Iterator<Integer> {

        @Override
        public boolean hasNext() {
          return true;
        }

        @Override
        public Integer next() {
          return field ^= 431 * 1493;
        }
      }

      @Override
      public Iterator<Integer> iterator() {
        return new Inner();
      }
    };
  }

  public static Iterable<Integer> innerInAnon2() {
    return new Iterable<Integer>() {
      public int field = 1491401;

      class I2 {
        class Inner implements Iterator<Integer> {

          @Override
          public boolean hasNext() {
            return true;
          }

          @Override
          public Integer next() {
            return field ^= 431 * 1493;
          }
        }
      }

      @Override
      public Iterator<Integer> iterator() {
        return new I2().new Inner();
      }
    };
  }
}
