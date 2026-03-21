package pkg;

public class TestAnonymousClassJ25 {
  public int value;

  public void consume(TestAnonymousClassJ25 inst) {

  }

  public void test() {
    consume(new TestAnonymousClassJ25() {
      @Override
      public void test() {
        System.out.println(value);
      }
    });
  }
}
