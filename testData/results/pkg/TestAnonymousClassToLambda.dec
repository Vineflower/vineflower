package pkg;

public class TestAnonymousClassToLambda {
   public Object test1(final Object o) {
      return new Object() {// 5
         public Runnable getRunnable() {
            return () -> System.out.println(o);// 7
         }
      };
   }
}

class 'pkg/TestAnonymousClassToLambda' {
   method 'test1 (Ljava/lang/Object;)Ljava/lang/Object;' {
      9      4
   }
}

class 'pkg/TestAnonymousClassToLambda$1' {
   method 'getRunnable ()Ljava/lang/Runnable;' {
      9      6
   }

   method 'lambda$getRunnable$0 (Ljava/lang/Object;)V' {
      0      6
      1      6
      2      6
      3      6
      4      6
      5      6
      6      6
      7      6
   }
}

Lines mapping:
5 <-> 5
7 <-> 7
