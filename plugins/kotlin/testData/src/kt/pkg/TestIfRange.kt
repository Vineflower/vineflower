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

  fun testIntStep(x: Int) {
    if (x in 1..100 step 2) {
      println(x)
    }
  }
  fun testIntStepY(x: Int, y: Int) {
    if (x in 1..100 step y) {
      println(x)
    }
  }

  fun testIntY(x: Int, y: Int) {
    if (x in 1..y) {
      println(x)
    }
  }

  fun testIntDownTo(x: Int) {
    if (x in 10 downTo 1) {
      println(x)
    }
  }

  fun testIntDownToStep(x: Int) {
    if (x in 10 downTo 1 step 2) {
      println(x)
    }
  }

  fun testIntUntil(x: Int) {
    if (x in 1 until 10) {
      println(x)
    }
  }

  fun testIntUntilStep(x: Int) {
    if (x in 1 until 100 step 2) {
      println(x)
    }
  }

  fun testIntUntilY(x: Int, y: Int) {
    if (x in 1 until y) {
      println(x)
    }
  }

  fun testIntUntilSelf(x: Int) {
    if (x in 1 until x) {
      println(x)
    }
  }
}