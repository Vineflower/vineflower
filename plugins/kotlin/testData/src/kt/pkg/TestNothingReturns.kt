package pkg

class TestNothingReturns {
  fun loop(): Nothing {
    while (true) {
      println("loop")
    }
  }

  fun test1(): Nothing {
    loop()
  }

  fun test2(): Long {
    test1()
  }

  fun test3(i: Int): Int {
    if (i == 0) {
      loop()
    }

    return test3(i - 1) + 1
  }

  fun test4() {
    loop()
    println("hello")
  }

  fun test5(s:String): String {
    return s.repeat(5) + loop()
  }

  fun test6(s: String?): String {
    return s ?: loop()
  }
}