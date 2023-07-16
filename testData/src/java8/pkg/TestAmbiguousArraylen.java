package pkg;

import java.util.Stack;
import java.util.Vector;

public class TestAmbiguousArraylen {
  protected final void myMethod(Vector myVector) {
    int myInt = myVector.firstElement() instanceof Stack ? ((Stack)myVector.firstElement()).size() : ((Object[])myVector.firstElement()).length;
    Object[] myObjects = myVector.firstElement() instanceof Stack ? ((Stack)myVector.get(0)).toArray() : (Object[])myVector.get(0);
    System.out.println(myObjects);
  }
}
