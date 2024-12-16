package pkg;

import java.util.HashMap;

public class TestBoxingSuperclass {

  public void referenceImplementationClass() {
    Implementation impl = new Implementation();
    impl.type(true);
    impl.type(1);
    impl.type(1F);
    impl.testAmbiguity(1F);
    impl.testAmbiguity(Float.valueOf(1F));
  }

  public void referenceSuperclassClass() {
    Parent impl = new Implementation();
    impl.type(true);
    impl.type(1);
    impl.type(1F);
    impl.testAmbiguity(1F);
    impl.testAmbiguity(Float.valueOf(1F));
  }

  public void referenceImplementationInterface() {
    InterfaceImplementation impl = new InterfaceImplementation();
    impl.type(true);
    impl.type(1);
    impl.type(1F);
    impl.testAmbiguity(1F);
    impl.testAmbiguity(Float.valueOf(1F));
  }

  public void referenceSuperclassInterface() {
    OtherTypes impl = new InterfaceImplementation();
    impl.type(true);
    impl.type(1);
    impl.type(1F);
    impl.testAmbiguity(1F);
    impl.testAmbiguity(Float.valueOf(1F));
  }

  public class Implementation extends Parent {

  }

  public abstract static class Parent {

    public void type(Boolean type) {
    }

    public void type(Integer type) {
    }

    public void type(Float type) {
    }

    public void testAmbiguity(float type) {
    }

    public void testAmbiguity(Float type) {
    }

  }

  public static class InterfaceImplementation implements OtherTypes {

  }

  interface OtherTypes {
    default void type(Boolean type) {
    }

    default void type(Integer type) {
    }

    default void type(Float type) {
    }

    default void testAmbiguity(Float type) {
    }

    default void testAmbiguity(float type) {
    }

  }

}
