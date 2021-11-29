package pkg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestNestedAnonymousClass {
   public Runnable r = new TestNestedAnonymousClass.B() {
      public void run() {
         ExecutorService ex = Executors.newFixedThreadPool(1);// 9
         ex.submit(new Runnable() {// 10
            public void run() {
               System.out.println("Hello");// 12
               b();// 13
               TestNestedAnonymousClass.this.a();// 14
            }// 15
         });
      }// 17
   };

   public void a() {
   }// 28

   public abstract class B implements Runnable {
      protected void b() {
      }// 23
   }
}

class 'pkg/TestNestedAnonymousClass$1' {
   method 'run ()V' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      9
      e      9
      f      9
      10      9
      11      9
      12      9
      14      16
   }
}

class 'pkg/TestNestedAnonymousClass$1$1' {
   method 'run ()V' {
      0      11
      1      11
      2      11
      3      11
      4      11
      5      11
      6      11
      7      11
      9      12
      a      12
      b      12
      c      12
      d      12
      e      12
      13      13
      14      13
      15      13
      16      13
      17      13
      18      13
      19      14
   }
}

class 'pkg/TestNestedAnonymousClass' {
   method 'a ()V' {
      0      20
   }
}

class 'pkg/TestNestedAnonymousClass$B' {
   method 'b ()V' {
      0      24
   }
}

Lines mapping:
9 <-> 9
10 <-> 10
12 <-> 12
13 <-> 13
14 <-> 14
15 <-> 15
17 <-> 17
23 <-> 25
28 <-> 21
