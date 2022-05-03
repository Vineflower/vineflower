package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestCollectionItr {
  public <T extends TestCollectionItr> List<T> method() {
    return (List<T>) list();
  }

  private List<TestCollectionItr> list() {
    return new ArrayList<>();
  }
}
