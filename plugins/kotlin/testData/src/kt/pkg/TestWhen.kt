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
}