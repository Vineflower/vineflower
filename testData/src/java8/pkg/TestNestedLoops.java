package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestNestedLoops {
    public void test() {
        List<String> list = new ArrayList<>();
        int i = 0;
        while (true) {
            while (i < 10) {
                for (String s : list) {
                    for (int j = 0; j < 20; j++) {
                        do {
                            s.substring(j);
                        } while (s.length() < j);
                    }
                }
                i++;
            }
        }
    }

    public void decomp() {
      List<String> list = new ArrayList<>();
      int i = 0;

      while(true) {
        while(i >= 10) {
        }

        for(String s : list) {
          for(int j = 0; j < 20; ++j) {
            while(true) {
              s.substring(j);
              if (s.length() >= j) {
                break;
              }
            }
          }
        }

        ++i;
      }
    }
}
