package pkg;

import org.vineflower.marker.CatchAllException;

public class TestTryFinallyMarkerExceptions {
  public void test0() {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      long l = 5;
      throw e;
    }
    {
      long l = 5;
    }
  }

  public void test1() {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void test2(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      if(i > 0) {
        System.out.println(i);
        return;
      }
      throw e;
    }
    {
      System.out.println("Finally");
      if(i > 0) {
        System.out.println(i);
        return;
      }
    }

    System.out.println("Bye");
  }
}
