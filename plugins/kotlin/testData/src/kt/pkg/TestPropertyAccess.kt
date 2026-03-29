package pkg

import ext.Person

class TestPropertyAccess {
  fun testGetter(person: Person) {
    println(person.name)
  }
  
  fun testSetter(person: Person) {
    person.age = 25
  }
  
  fun testOperation(person: Person) {
    person.age += 4
  }
}