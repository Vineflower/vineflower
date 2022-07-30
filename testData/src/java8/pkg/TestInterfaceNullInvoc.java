package pkg;

public interface TestInterfaceNullInvoc {
  void invoc(String str);

  void invocOverload(String str);
  void invocOverload(Number num);

  default void test() {
    invoc(null);
    invocOverload((String) null);
    invocOverload((Number) null);
  }
}
