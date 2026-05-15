package pkg;

import org.vineflower.marker.CatchAllException;

public class TestTryFinallyMarkerExceptionsInvalid {
  public Object testReturn() {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      return e; // return instead of throw
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
    return null;
  }


  public void testLogged(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      // eg logger injection
      System.out.println(e.getMessage());

      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testWeirdCatch(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("NPE");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }

  public void testWeirdCatch2(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("Finally");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testWeirdCatch3(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("Finally");
      System.out.println("NPE: " + npe.getMessage());
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testBlockReuse(int i) {
    // while matching basic blocks, just because a "sample" block has been seen before
    // does not guarantee it matches with the block we are inspecting.
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      if(i > 0){
        System.out.println("Cool 1");
      } else {
        System.out.println("Cool 2");
      }
      if(i < 0); // break up basic block
      throw e;
    }
    {
      System.out.println("Finally");
      if(i > 0);
      System.out.println("Cool 1");
      if(i < 0); // break up basic block
    }

    System.out.println("Bye");
  }
}
