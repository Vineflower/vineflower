package pkg;

public class TestGenericCastCall {
  public class Inner<T> {
  }

  public class Inner2<T> extends Inner<T> {
    T val() {
      return null;
    }
  }

  public Inner<Boolean> inner;

  private Inner<Boolean> get() {
    return inner;
  }

  public boolean test(boolean b) {
    if (!((Inner2<Boolean>)inner).val() && b) {
      return true;
    }

    System.out.println(b);

    return false;
  }

  public boolean test1(boolean b) {
    if (!((Inner2<Boolean>)get()).val() && b) {
      return true;
    }

    System.out.println(b);

    return false;
  }
}
