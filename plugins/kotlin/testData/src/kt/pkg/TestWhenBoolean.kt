package pkg

class TestWhenBoolean {
  fun testIf(a: Int, b: Int, c: Int, d: Int) {
    if (when {
        a == 1 -> true
        b == 2 -> true
        c == 3 -> false
        a > b -> true
        c in a..b -> false
        d == 7 -> b == 100 - a
        a == -100 -> when {
          b == 0 -> true
          c == 55 -> false
          d == 66 -> c in 25..33
          else -> c < d && a < b
        }
        else -> true
      }
    ) {
      println("hello")
    }
  }
}