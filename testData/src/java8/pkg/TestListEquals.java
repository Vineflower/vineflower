package pkg;

import java.util.Collections;
import java.util.List;

public class TestListEquals {
  public class Inner<T> {

  }

  public boolean equals(List<Inner<? extends Number>> o) {
    return o == Collections.<Inner<? extends Number>>emptyList();
  }
}
