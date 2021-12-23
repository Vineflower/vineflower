package pkg;

public interface TestInterfaceNullInvoc {
   void invoc(String var1);

   void invocOverload(String var1);

   void invocOverload(Number var1);

   default void test() {
      this.invoc(null);// 10
      this.invocOverload((String)null);// 11
      this.invocOverload((Number)null);// 12
   }// 13
}

class 'pkg/TestInterfaceNullInvoc' {
   method 'test ()V' {
      0      10
      1      10
      2      10
      3      10
      4      10
      5      10
      6      10
      7      11
      8      11
      9      11
      a      11
      b      11
      c      11
      d      11
      e      11
      f      11
      10      11
      11      12
      12      12
      13      12
      14      12
      15      12
      16      12
      17      12
      18      12
      19      12
      1a      12
      1b      13
   }
}

Lines mapping:
10 <-> 11
11 <-> 12
12 <-> 13
13 <-> 14
