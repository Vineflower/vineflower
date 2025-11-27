package pkg;

import java.util.Collection;
import java.util.List;

// Taken roughly from: https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science)
public class TestGenericLattice {
  static class Animal {}

  static class Cat extends Animal {}

  // these need to be fields to not get deleted by javac
  Collection<Cat> cc;
  List<? extends Cat> lec;
  List<? super Cat> lsc;
  Collection<? extends Cat> cec;
  Collection<? super Cat> csc;
  Collection<? extends Animal> cea;
  Collection<? super Animal> csa;
  List<? extends Animal> lea;
  List<? super Animal> lsa;
  List<?> w1;
  List r1;

  public void testAssignable() {
    List<Animal> la = null;
    List<Cat> lc = null;
    List<? extends Cat> lec = null;
    List<? super Cat> lsc = null;
    List<? super Animal> lsa = null;
    List<? extends Animal> lea = null;
    this.cc = lc; // List <: Collection

    // Assign into wildcards
    this.lec = lc;
    this.lsc = lc;

    // into supertype
    this.cec = lec;
    this.cec = lc;
    this.csc = lsc;
    this.csc = lc;

    // Into supertype of parameter
    this.lea = lec;
    this.lea = la;

    this.lsa = la;
    this.csa = la;

    this.cea = lc;
    this.cea = lc;
    this.cea = la;
    this.cea = la;

    // contravariance nightmare zone
    this.lsc = lsa;

    // rawtypes and wildcards
    this.w1 = lc;
    this.w1 = lec;
    this.w1 = lsc;
    this.w1 = lea;
    this.w1 = lsa;

    this.r1 = lc;
  }
}
