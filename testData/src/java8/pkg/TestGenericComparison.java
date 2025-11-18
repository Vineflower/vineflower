package pkg;

public class TestGenericComparison {
  Generic<?> generic = new Generic<>();

  void test() {
    if (dum(generic) != dum(generic)) {

    }
  }

  <T> T dum(Generic<T> dum) {
    return null;
  }

  static class Generic<T> {

  }

}
