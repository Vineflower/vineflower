package pkg;

public class TestVarMergeSupertype {
  public void myMethod() {
    MyInterface rmcp = null;
    rmcp = getMyInterfaceImpl();
  }

  public void myMethod2() {
    MyInterfaceImpl rmcp = null;
    rmcp = getMyInterfaceImpl();
  }

  public MyInterfaceImpl getMyInterfaceImpl() {
    return null;
  }

  class MyInterfaceImpl implements MyInterface {
  }

  interface MyInterface {
  }
}
