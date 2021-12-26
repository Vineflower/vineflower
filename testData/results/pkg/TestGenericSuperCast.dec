package pkg;

public class TestGenericSuperCast {
   public <T> Class<T> test(TestGenericSuperCast.Inner<T> inner) {
      Class<T> t = (Class<T>)inner.get();// 11
      return (Class<T>)inner.get();// 12
   }

   public <T> Class<? extends T> test1(TestGenericSuperCast.Inner<T> inner) {
      Class<? extends T> t = (Class<? extends T>)inner.get();// 16
      return (Class<? extends T>)inner.get();// 17
   }

   public class Inner<T> {
      public Class<? super T> get() {
         return null;// 6
      }
   }
}

class 'pkg/TestGenericSuperCast' {
   method 'test (Lpkg/TestGenericSuperCast$Inner;)Ljava/lang/Class;' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      5
      6      5
      7      5
      8      5
      9      5
   }

   method 'test1 (Lpkg/TestGenericSuperCast$Inner;)Ljava/lang/Class;' {
      0      9
      1      9
      2      9
      3      9
      4      9
      5      10
      6      10
      7      10
      8      10
      9      10
   }
}

class 'pkg/TestGenericSuperCast$Inner' {
   method 'get ()Ljava/lang/Class;' {
      0      15
      1      15
   }
}

Lines mapping:
6 <-> 16
11 <-> 5
12 <-> 6
16 <-> 10
17 <-> 11
