package pkg

class TestClassicStringInterpolation {
  fun stringInterpolation(x: Int, y: String) {
      println("$x $y")
  }

  fun testConstant(x: Int) {
    println("$x ${5}")
  }

  fun testExpression(x: Int) {
      println("$x ${x + 1}")
  }

  val x = 5
  fun testProperty() {
      println("$x!")
  }

  fun testLiteralClass() {
      println("${TestClassicStringInterpolation::class.java}!")
  }
}
