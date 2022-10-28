package pkg;

public class TestSynchronizedThrow {
  public void test() {
    synchronized (this) {
      throw new RuntimeException();
    }
  }

  public void test1() {
    System.out.println("Hi");
    synchronized (this) {
      System.out.println("Hello");
      throw new RuntimeException();
    }
  }

  public void testConditionalThrow(boolean b) {
    synchronized (this) {
      if (b) {
        throw new RuntimeException();
      }
    }
  }

  public void testConditionalThrow2(boolean b) {
    System.out.println("Hi");
    synchronized (this) {
      System.out.println("Hello");
      if (b) {
        throw new RuntimeException();
      }
      System.out.println("World");
    }
    System.out.println("Bye");
  }

  public void testLoopThrow(boolean b) {
    synchronized (this) {
      while (b) {
        System.out.println("oh");
      }
      throw new RuntimeException();
    }
  }

  public void testInfiniteLoopOrThrow(boolean b) {
    synchronized (this) {
      if (b) {
        while (true) {
          System.out.println("looooooooooop");
        }
      }
      throw new RuntimeException();
    }
  }
}
