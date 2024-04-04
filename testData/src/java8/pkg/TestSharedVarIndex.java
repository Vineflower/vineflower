package pkg;

import java.util.List;

public class TestSharedVarIndex {
  public void StringArrayVsString(){
    {
      String[] a = new String[0];
    }

    String b = "lol";
  }

  public void lambda(List<Integer> l) {
    {
      int i = 0;
      System.out.println(i);
    }
    l.forEach(i -> System.out.println(i + 1));
  }
}
