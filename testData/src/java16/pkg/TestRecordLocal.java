package pkg;

import java.util.List;

public class TestRecordLocal {
    public Object test(List<Integer> list) {
        record Rec(TestRecordLocal a, List<Integer> b) {}
  
        return new Rec(this, list);
    }
}
