package pkg;

import java.util.List;

public class TestRecordLocal {
   public Object test(List<Integer> list) {
      record Rec(TestRecordLocal a, List<Integer> b) {
      }

      return new Rec(this, list);// 9
   }
}

class 'pkg/TestRecordLocal' {
   method 'test (Ljava/util/List;)Ljava/lang/Object;' {
      4      9
      5      9
      9      9
   }
}

Lines mapping:
9 <-> 10
