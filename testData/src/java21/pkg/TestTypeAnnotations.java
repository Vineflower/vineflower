package pkg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

public class TestTypeAnnotations {
  @Target(ElementType.TYPE_USE)
  public @interface Anno {

  }

  public class Inner {
    public TestTypeAnnotations.Inner.@Anno InnerRec aa;
    public TestTypeAnnotations.Inner.@Anno InnerInnerStatic ab;
    public @Anno TestTypeAnnotations.Inner.InnerInner ac;
    public TestTypeAnnotations.@Anno Inner.InnerInner ad;
    public TestTypeAnnotations.Inner. @Anno InnerInner ae;

    public record InnerRec() {
      public static TestTypeAnnotations.Inner.@Anno InnerRec.A ba;
      public static TestTypeAnnotations.Inner.InnerRec.@Anno A bb;
      public class A {

      }
    }

    public static class InnerInnerStatic {
      // From the inner above
      public static TestTypeAnnotations.Inner.@Anno InnerRec.A ca;
      public static TestTypeAnnotations.Inner.InnerRec.@Anno A cb;

      public static TestTypeAnnotations.Inner.InnerInnerStatic.@Anno B cc;
      public static TestTypeAnnotations.Inner.@Anno InnerInnerStatic.B cd;

      public class B {

      }
    }

    public class InnerInner {

    }
  }

  public static class InnerStatic {
    public class Inner2 {

    }

    public static class Inner2Static {
    }


  }

  public static class G1<T> {

  }

  public static class G2<K, V> {

  }

  public @Anno TestTypeAnnotations.Inner a;
  public TestTypeAnnotations.@Anno Inner b;
  public TestTypeAnnotations.@Anno InnerStatic c;
  public @Anno TestTypeAnnotations[] d;
  public TestTypeAnnotations @Anno [] e;
  public TestTypeAnnotations.@Anno InnerStatic.Inner2 f;
  public TestTypeAnnotations.InnerStatic.@Anno Inner2 g;
  public TestTypeAnnotations.InnerStatic.@Anno Inner2Static g_1;
  public TestTypeAnnotations.@Anno Inner[] h;
  public TestTypeAnnotations.Inner @Anno [] i;
  public @Anno TestTypeAnnotations[][] j;
  public TestTypeAnnotations.Inner[] @Anno [] k;
  public TestTypeAnnotations.Inner @Anno [][] k_1;
  public TestTypeAnnotations.InnerStatic.Inner2 @Anno [] l;
  public TestTypeAnnotations.InnerStatic.@Anno Inner2[] m;
  public @Anno G1<TestTypeAnnotations> n;
  public G1<@Anno TestTypeAnnotations> o;
  public @Anno G1<TestTypeAnnotations>[] p;
  public G1<@Anno TestTypeAnnotations[]> q;
  public G1<TestTypeAnnotations @Anno []> r;
  public G1<@Anno ? extends TestTypeAnnotations> s;
  public G1<? extends @Anno TestTypeAnnotations> t;
  public G1<@Anno ?> u;
  public @Anno MethodHandle v; // import test
  public G2<@Anno String, Integer> w;
  public G2<String, @Anno Integer> x;

  public @Anno TestTypeAnnotations.Inner a() {
    return null;
  }

  public TestTypeAnnotations.@Anno Inner b() {
    return null;
  }

  public TestTypeAnnotations.@Anno InnerStatic c() {
    return null;
  }

  public @Anno TestTypeAnnotations[] d() {
    return null;
  }

  public TestTypeAnnotations @Anno [] e() {
    return null;
  }

  public void varargs(@Anno Object... args) {

  }

  public void varargs(int v, @Anno Object... args) {

  }

  public void varargs2(Object @Anno ... args) {

  }

  public void varargs2(int v, Object @Anno ... args) {

  }

  public void varargs3a(Object[] @Anno ... args) {

  }

  public void varargs3b(Object @Anno []... args) {

  }
}
