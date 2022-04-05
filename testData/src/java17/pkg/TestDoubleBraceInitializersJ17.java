package pkg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestDoubleBraceInitializersJ17 {
  public void test() {
    List<String> list = new ArrayList<>() {{
      add("foo");
      add("bar");
      add("baz");
    }};
    System.out.println(list);
  }

  public void test2() {
    Date date = new Date() {{
      setTime(123456789 * 1000L);
    }};
    System.out.println(date);
  }

  public enum TestEnum {
    A {{
      System.out.println("A");
    }},
    B {{
      System.out.println("B");
    }}
  }

  public enum TestEnum1 {
    A {{
      System.out.println("A");
      this.field = 1;
    }},
    B {{
      System.out.println("B");
      this.field = 2;
    }};

    public int field;
    TestEnum1() {

    }
  }
}
