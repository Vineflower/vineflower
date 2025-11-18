package pkg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

public class TestTypeAnnotationsTokens {
  @Target(ElementType.TYPE_USE)
  public @interface Anno {

  }

  public class Inner {
  }

  public static class InnerStatic {
    public class Inner2 {

    }

    public static class Inner2Static {
    }


  }

  public @Anno TestTypeAnnotationsTokens.Inner a;
  public TestTypeAnnotationsTokens.@Anno Inner b;
  public TestTypeAnnotationsTokens.@Anno InnerStatic c;
  public @Anno TestTypeAnnotationsTokens[] d;
  public TestTypeAnnotationsTokens @Anno [] e;
  public TestTypeAnnotationsTokens.@Anno InnerStatic.Inner2 f;
  public TestTypeAnnotationsTokens.InnerStatic.@Anno Inner2 g;
  public TestTypeAnnotationsTokens.InnerStatic.@Anno Inner2Static g_1;
}
