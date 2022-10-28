package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestGenericCast {
  private List l1 = new ArrayList();
  private List<String> l2 = new ArrayList<>();

  public String[] test1() {
    return (String[]) l1.toArray(new String[0]);
  }

  public String[] test2() {
    return l2.toArray(new String[0]);
  }
}
