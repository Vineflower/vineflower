package pkg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class TestTypeAnnotations {
  @Target(ElementType.TYPE_USE)
  public @interface Anno {

  }

  public class Inner {

  }

  public static class InnerStatic {

  }

  public @Anno TestTypeAnnotations.Inner a;
  public TestTypeAnnotations.@Anno Inner b;
  public TestTypeAnnotations.@Anno InnerStatic c;
  public @Anno TestTypeAnnotations[] d;
  public TestTypeAnnotations @Anno [] e;

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
}
