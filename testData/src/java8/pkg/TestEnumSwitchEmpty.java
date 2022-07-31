package pkg;

import java.lang.annotation.ElementType;

public class TestEnumSwitchEmpty {
  public void test() {
    ElementType et = ElementType.TYPE;
    ElementType et2 = ElementType.TYPE;
    switch (et) {
    }
    switch (et2) {
    }
  }
}
