package pkg;

public class TestTryCatchThrowable {

  void testEmptyCatch(){
    try {
      System.out.println("Hi");
    } catch (Throwable ignored){
    }
  }

  void testEmptyCatchWithTail(){
    try {
      System.out.println("Hi");
    } catch (Throwable ignored){
    }

    System.out.println("tail");
  }

  String testCatchWithReturn(){
    try {
      System.out.println("Hi");
      return "5";
    } catch (Throwable ignored){
      return null;
    }
  }

  String testCatchWithReturnAndTail(){
    try {
      System.out.println("Hi");
    } catch (Throwable ignored){
      return null;
    }
    System.out.println("Ho");
    return "bye";
  }

  String testCatchNested(){
    try {
      System.out.println("Hi");
      return "hello";
    } catch (Throwable ignored){
      try {
        System.out.println("Hi!");
        return "5";
      } catch (Throwable throwable){
        return throwable.getMessage();
      }
    }
  }

  String testCatchWithRethrow(){
    try {
      System.out.println("Hi");
      return "5";
    } catch (Throwable throwable){
      System.out.println("Oh no");
      throw throwable;
    }
  }


  void testCatchAndOtherEmptyCatch(){
    try {
      System.out.println("Hi");
    } catch (RuntimeException ignored) {

    } catch (Throwable ignored){
      System.out.println("Oh no");
    }
  }

  void testCatchAndOtherNonEmptyCatch(){
    try {
      System.out.println("Hi");
    } catch (RuntimeException ignored) {
      System.out.println("Hello");
    } catch (Throwable ignored){
      System.out.println("Oh no");
    }
  }
}

