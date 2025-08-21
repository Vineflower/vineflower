package pkg;

public interface TestInterfaceSubclass {
   void doThing();

   int doOtherThing();

   abstract class AbstractClass implements TestInterfaceSubclass {
      public abstract double doDoubleThing();
   }

   class Multiple extends TestInterfaceSubclass.AbstractClass implements TestInterfaceSubclass {
      @Override
      public void doThing() {
         System.out.println("Hello");// 29
      }// 30

      @Override
      public int doOtherThing() {
         return 1;// 34
      }

      @Override
      public double doDoubleThing() {
         return 2.452;// 39
      }
   }

   class Subclass implements TestInterfaceSubclass {
      @Override
      public void doThing() {
         System.out.println("Hi");// 12
      }// 13

      @Override
      public int doOtherThing() {
         return 0;// 17
      }
   }
}

class 'pkg/TestInterfaceSubclass$Multiple' {
   method 'doThing ()V' {
      0      14
      1      14
      2      14
      3      14
      4      14
      5      14
      6      14
      7      14
      8      15
   }

   method 'doOtherThing ()I' {
      0      19
      1      19
   }

   method 'doDoubleThing ()D' {
      0      24
      1      24
      2      24
      3      24
   }
}

class 'pkg/TestInterfaceSubclass$Subclass' {
   method 'doThing ()V' {
      0      31
      1      31
      2      31
      3      31
      4      31
      5      31
      6      31
      7      31
      8      32
   }

   method 'doOtherThing ()I' {
      0      36
      1      36
   }
}

Lines mapping:
12 <-> 32
13 <-> 33
17 <-> 37
29 <-> 15
30 <-> 16
34 <-> 20
39 <-> 25
