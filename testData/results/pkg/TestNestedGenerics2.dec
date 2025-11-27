package pkg;

public class TestNestedGenerics2 {
   public static <T, R> R test(T t) {
      var instance = new Object() {// 5
         R run(T t) {
            return null;// 7
         }
      };
      return (R)instance.run(t);// 10
   }
}

class 'pkg/TestNestedGenerics2' {
   method 'test (Ljava/lang/Object;)Ljava/lang/Object;' {
      7      4
      8      9
      9      9
      a      9
      b      9
      c      9
      d      9
   }
}

class 'pkg/TestNestedGenerics2$1' {
   method 'run (Ljava/lang/Object;)Ljava/lang/Object;' {
      0      6
      1      6
   }
}

Lines mapping:
5 <-> 5
7 <-> 7
10 <-> 10
