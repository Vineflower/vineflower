package pkg

class TestIfRange {
  fun testInt(x: Int) {
    if (x in 1..10) {
      println(x)
    }
  }

  fun testChar(x: Char) {
    if (x in 'a'..'z') {
      println(x)
    }
  }

  fun testInvertedInt(x: Int) {
    if (x !in 1..10) {
      println(x)
    }
  }
}