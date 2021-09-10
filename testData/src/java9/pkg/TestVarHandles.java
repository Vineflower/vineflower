package pkg;

import java.lang.invoke.*;

public class TestVarHandles {
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public void test1() throws ReflectiveOperationException {
    VarHandle lookupHandle = LOOKUP.findStaticVarHandle(TestVarHandles.class, "LOOKUP", MethodHandles.Lookup.class);
    MethodHandles.Lookup lookup = (MethodHandles.Lookup) lookupHandle.get();
  }

  public void test2() {
    String[] arr = {"a"};
    VarHandle elementHandle = MethodHandles.arrayElementVarHandle(String[].class);
    boolean success = elementHandle.compareAndSet(arr, 0, "a", "b");
    System.out.println(success);
  }
}
