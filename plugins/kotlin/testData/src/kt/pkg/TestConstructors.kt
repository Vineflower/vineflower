package pkg

class TestConstructors private constructor() {
  constructor(a: Int) : this() {
    println("a = $a")
  }

  constructor(a: Int, b: Int) : this(a) {
    println("b = $b")
  }
}