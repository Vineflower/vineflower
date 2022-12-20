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

  fun testIntStep() {
    for (i in 1..10 step 2) {
      println(i)
    }
  }

  fun testIntStepX(x: Int) {
    for (i in 1..100 step x) {
      println(i)
    }
  }

  fun testIntDownTo() {
    for (i in 10 downTo 1) {
      println(i)
    }
  }

  fun testIntDownToStep() {
    for (i in 10 downTo 1 step 2) {
      println(i)
    }
  }

  fun testIntDownToStepX(x: Int) {
    for (i in 100 downTo 1 step x) {
      println(i)
    }
  }

  fun testUntil() {
    for (i in 1 until 10) {
      println(i)
    }
  }

  fun testUntilStep() {
    for (i in 1 until 100 step 2) {
      println(i)
    }
  }

  fun testUntilStepX(x: Int) {
    for (i in 1 until 100 step x) {
      println(i)
    }
  }

  fun testIntY(x: Int, y: Int) {
    for (i in x..y) {
      println(i)
    }
  }

  fun testIntYStep(x: Int, y: Int) {
    for (i in x..y step 2) {
      println(i)
    }
  }

  fun testIntYStepX(x: Int, y: Int, z: Int) {
    for (i in x..y step z) {
      println(i)
    }
  }
}