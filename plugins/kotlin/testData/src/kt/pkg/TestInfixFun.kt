package pkg

class TestInfixFun {
  fun test() {
    infix fun Int.times(str: String) = str.repeat(this)

    println(2 times "Bye ")
  }

  infix fun Int.mult(str: String) = str.repeat(this)

  fun testOuter() {

    println(2 mult "Bye ")
  }

  fun testDuplicate() {
    infix fun Int.mult(str: String) = str.repeat(this + 1)

    println(2 mult "Bye ")
  }
}