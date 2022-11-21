package pkg

class TestForRange {
  fun testInt() {
    for (i in 1..10) {
      println(i)
    }
  }

  fun testChar() {
    for (c in 'a'..'z') {
      println(c)
    }
  }
}