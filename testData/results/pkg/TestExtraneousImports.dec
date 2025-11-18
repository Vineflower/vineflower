package pkg;

import java.util.Map;

public class TestExtraneousImports {
   public void myMethod(Map<String, String> map) {
      map.entrySet().forEach(entry -> System.out.println((String)entry.getValue()));// 7
   }// 8
}

class 'pkg/TestExtraneousImports' {
   method 'myMethod (Ljava/util/Map;)V' {
      0      6
      1      6
      2      6
      3      6
      4      6
      5      6
      b      6
      c      6
      d      6
      e      6
      f      6
      10      7
   }

   method 'lambda$myMethod$0 (Ljava/util/Map$Entry;)V' {
      0      6
      1      6
      2      6
      3      6
      4      6
      5      6
      6      6
      7      6
      8      6
      9      6
      a      6
      b      6
      c      6
      d      6
      e      6
      f      6
   }
}

Lines mapping:
7 <-> 7
8 <-> 8
