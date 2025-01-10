package pkg;

import java.util.Map;

public class TestExtraneousImports {
  public void myMethod(Map<String, String> map) {
    map.entrySet().forEach(entry -> System.out.println(entry.getValue()));
  }
}
