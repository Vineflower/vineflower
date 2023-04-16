package pkg;

import java.util.HashMap;
import java.util.Iterator;

public class TestStaticIterator {
   private static HashMap hashMap = new HashMap();

   private static Iterator iterator() {
      return hashMap.values().iterator();// 10
   }

   private static Iterable iterable() {
      return hashMap.values();// 14
   }

   public static void test() {
      Iterator iter = iterator();// 18

      while (iter.hasNext()) {// 19
         TestStaticIterator var1 = (TestStaticIterator)iter.next();// 20
      }
   }// 22

   public static void test1() {
      for (Object o : iterable()) {// 25
         System.out.println(o);// 26
      }
   }// 28
}

class 'pkg/TestStaticIterator' {
   method 'iterator ()Ljava/util/Iterator;' {
      0      9
      1      9
      2      9
      3      9
      4      9
      5      9
      6      9
      7      9
      8      9
      9      9
      a      9
      b      9
   }

   method 'iterable ()Ljava/lang/Iterable;' {
      0      13
      1      13
      2      13
      3      13
      4      13
      5      13
      6      13
   }

   method 'test ()V' {
      0      17
      1      17
      2      17
      3      17
      4      19
      5      19
      6      19
      7      19
      8      19
      9      19
      d      20
      e      20
      f      20
      10      20
      11      20
      12      20
      13      20
      14      20
      15      20
      16      20
      1a      22
   }

   method 'test1 ()V' {
      0      25
      1      25
      2      25
      3      25
      4      25
      5      25
      6      25
      7      25
      8      25
      12      25
      13      25
      14      25
      15      25
      16      25
      17      25
      18      25
      19      26
      1a      26
      1b      26
      1c      26
      1d      26
      1e      26
      1f      26
      23      28
   }
}

Lines mapping:
10 <-> 10
14 <-> 14
18 <-> 18
19 <-> 20
20 <-> 21
22 <-> 23
25 <-> 26
26 <-> 27
28 <-> 29
Not mapped:
21
27
