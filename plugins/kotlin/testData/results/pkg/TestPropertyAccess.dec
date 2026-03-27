package pkg

import ext.Person

public class TestPropertyAccess {
   public fun testGetter(person: Person) {
      System.out.println(person.name)// 7
   }// 8

   public fun testSetter(person: Person) {
      person.age = 25// 11
   }// 12

   public fun testOperation(person: Person) {
      person.age = person.age + 4// 15
   }// 16
}

class 'pkg/TestPropertyAccess' {
   method 'testGetter (Lext/Person;)V' {
      6      6
      7      6
      8      6
      9      6
      a      6
      b      6
      c      6
      e      6
      f      6
      10      6
      11      7
   }

   method 'testSetter (Lext/Person;)V' {
      6      10
      7      10
      8      10
      9      10
      a      10
      b      10
      c      11
   }

   method 'testOperation (Lext/Person;)V' {
      6      14
      7      14
      8      14
      9      14
      a      14
      b      14
      c      14
      d      14
      e      14
      f      14
      10      15
   }
}

Lines mapping:
7 <-> 7
8 <-> 8
11 <-> 11
12 <-> 12
15 <-> 15
16 <-> 16
