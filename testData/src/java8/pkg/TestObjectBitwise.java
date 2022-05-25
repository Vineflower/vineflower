package pkg;

public class TestObjectBitwise {
  public boolean test(int i) {
    return (((Long)obj()) & i) == 0;
  }

  public Object obj() {
    return 100000L;
  }
}
