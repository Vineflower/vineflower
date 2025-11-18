package pkg;

import java.lang.StringTemplate.Processor;
import java.lang.runtime.TemplateRuntime;

public class TestCustomProcessor {
   static Processor<Object, Error> processor;

   public void test() {
      String s = "Hello";// 7
      int i = 42;// 8
      double d = 3.14159;// 9
      Object result = processor.process(TemplateRuntime.newStringTemplate<"process","Text: "," "," ","">(s, i, d));// 11
   }// 12
}

class 'pkg/TestCustomProcessor' {
   method 'test ()V' {
      0      9
      1      9
      2      9
      3      10
      4      10
      5      10
      6      11
      7      11
      8      11
      9      11
      a      12
      b      12
      c      12
      d      12
      e      12
      f      12
      10      12
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
      1b      12
      1c      13
   }
}

Lines mapping:
7 <-> 10
8 <-> 11
9 <-> 12
11 <-> 13
12 <-> 14
