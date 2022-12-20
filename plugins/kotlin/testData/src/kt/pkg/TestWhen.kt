package pkg

class TestWhen {
  fun testStatement(obj: Any) {
    when (obj) {
      1 -> println("1")
      "2" -> println("2")
      is Double -> println("Double")
      !is Long -> println("Not Long")
      else -> println("else")
    }
  }

  fun testExpression(obj: Any): Int {
    return when (obj) {
      1 -> 1
      is Double -> 2
      "4" -> 4
      !is Long -> 3
      else -> 5
    }
  }

  fun testStatement2(a: Any, b: Any): Unit {
    when {
      a == 15 -> println("a == 15")
      a == "!!" -> println("a == !!")
      a is Int -> println("a is Int")
      a is String -> println("a is String")
      b is Double -> println("b is Double")
      a is Unit -> println("a is Unit")
      else -> println("else")
    }
  }

  fun booleanNightmares(a: Boolean, b: Boolean, c:Boolean, d:Boolean, e:Boolean, f:Boolean, g:Boolean) {
    when(a) {
      (b != c) -> println("-_-")
      (b && !e) -> println("xxx")
      (!a || d) -> println("ohno")
      false -> println("NIGHTMARE")
      (g || (e && (f != c))) -> println("hello")
      else -> println("else")
    }
  }
}