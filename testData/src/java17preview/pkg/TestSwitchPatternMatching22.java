package pkg;

import java.lang.annotation.ElementType;
import java.util.Random;
import java.util.Scanner;

import java.util.*;
import java.lang.annotation.*;

public class TestSwitchPatternMatching22 {
  public void test1() {
    try {
      System.out.println("hello");
    } catch (Exception e) {
      switch (new Object()) {
        default:
          System.out.println("Not a short");
          throw new RuntimeException();
        case Short vvv18:
      }
    } finally {
      System.out.println("world");
    }
  }

  public void test2() {
    try {
      System.out.println("hello");
    } catch (Exception e) {
      switch (new Object()) {
        default:
          System.out.println("Not a short");
          throw new RuntimeException();
        case Short vvv18:
      }
    } finally {
      throw new RuntimeException();
    }
  }

  public void test1Null() {
    try {
      System.out.println("hello");
    } catch (Exception e) {
      switch (new Object()) {
        default:
          System.out.println("Not a short");
          throw new RuntimeException();
        case null:
        case Short vvv18:
      }
    } finally {
      System.out.println("world");
    }
  }

  public void test2Null() {
    try {
      System.out.println("hello");
    } catch (Exception e) {
      switch (new Object()) {
        default:
          System.out.println("Not a short");
          throw new RuntimeException();
        case null:
        case Short vvv18:
      }
    } finally {
      throw new RuntimeException();
    }
  }
}
