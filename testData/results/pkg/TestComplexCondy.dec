package pkg;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class TestComplexCondy {
   public static Integer field = 1;// 14 15
   public Integer instance;
   public int primitive;

   public static VarHandle testVarHandle() {
      return /* $VF: constant dynamic replaced with non-lazy method call */ MethodHandles.lookup().findVarHandle(TestCondy.class, "instance", Integer.class);// 26
   }

   public static VarHandle testStaticVarHandle() {
      return /* $VF: constant dynamic replaced with non-lazy method call */ MethodHandles.lookup().findStaticVarHandle(TestCondy.class, "field", Integer.class);// 36
   }

   public static VarHandle testPrimitiveVarHandle() {
      return /* $VF: constant dynamic replaced with non-lazy method call */ MethodHandles.lookup().findVarHandle(TestCondy.class, "primitive", int.class);// 46
   }

   public static VarHandle testArrayVarHandle() {
      return /* $VF: constant dynamic replaced with non-lazy method call */ MethodHandles.arrayElementVarHandle(TestCondy[].class);// 55
   }
}

class 'pkg/TestComplexCondy' {
   method '<clinit> ()V' {
      0      6
      1      6
      2      6
      3      6
   }

   method 'testVarHandle ()Ljava/lang/invoke/VarHandle;' {
      2      11
   }

   method 'testStaticVarHandle ()Ljava/lang/invoke/VarHandle;' {
      2      15
   }

   method 'testPrimitiveVarHandle ()Ljava/lang/invoke/VarHandle;' {
      2      19
   }

   method 'testArrayVarHandle ()Ljava/lang/invoke/VarHandle;' {
      2      23
   }
}

Lines mapping:
14 <-> 7
15 <-> 7
26 <-> 12
36 <-> 16
46 <-> 20
55 <-> 24
Not mapped:
16
22
32
42
52
