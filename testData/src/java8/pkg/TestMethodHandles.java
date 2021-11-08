package pkg;

import java.io.PrintStream;
import java.lang.invoke.*;

public class TestMethodHandles {
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public void test1() throws Throwable {
    MethodHandle abs = LOOKUP.findStatic(Math.class, "abs", MethodType.methodType(long.class, long.class));
    int a = -5;
    long b = (long) abs.invokeExact((long) a);
    System.out.println(b);
  }

  public int test2() throws Throwable {
    MethodHandle abs = LOOKUP.findStatic(Math.class, "abs", MethodType.methodType(long.class, long.class));
    int a = -5;
    return (int) (long) abs.invokeExact((long) a);
  }

  public void test3() throws Throwable {
    MethodHandle abs = LOOKUP.findStatic(Math.class, "abs", MethodType.methodType(long.class, long.class));
    int a = -5;
    long b = (long) abs.invoke((long) a);
    System.out.println(b);
  }

  public int test4() throws Throwable {
    MethodHandle abs = LOOKUP.findStatic(Math.class, "abs", MethodType.methodType(long.class, long.class));
    int a = -5;
    return (int) (long) abs.invoke((long) a);
  }

  public void test5() throws Throwable {
    MethodHandle println = LOOKUP.findVirtual(PrintStream.class, "println", MethodType.methodType(void.class, long.class));
    int a = -5;
    println.invokeExact(System.out, (long) a);
  }
}
