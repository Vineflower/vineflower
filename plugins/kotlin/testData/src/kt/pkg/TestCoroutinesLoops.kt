package pkg

class TestCoroutinesLoops {

  suspend fun testA(a: Int): Int {
    return 0
  }

  suspend fun testTruthMachine(a: Int): Int {
    if (a == 0) {
      return 0
    }

    while (true) {
      testA(a)
    }
  }

  suspend fun testForLoop(a: Int, b: Int) : Int {
    for (i in a..b) {
      if (testA(i) == 17) {
        return i
      }

      if (i == 17) {
        testTruthMachine(a)
      }
    }

    return 0
  }

  suspend fun testForLoopWithBreak(a: Int, b: Int) : Int {
    for (i in a..b) {
      if (testA(i) == 17) {
        break
      }

      if (i == 17) {
        testTruthMachine(a)
      }
    }

    return 0
  }

  suspend fun testForLoopWithContinue(a: Int, b: Int) : Int {
    for (i in a..b) {
      if (testA(i) == 17) {
        continue
      }

      if (i == 17) {
        testTruthMachine(a)
      }
    }

    return 0
  }

  suspend fun testNestedForLoop(a: Int, b: Int) : Int {
    for (i in a..b) {
      for (j in a..b) {
        if (testA(i * j) == 17) {
          return i
        }

        if (i == 17 * j) {
          testTruthMachine(a + j)
        }

        if (testA(j) == 17) {
          break
        }

        if (j == 17 - i) {
          testTruthMachine(a)
        }
      }
    }

    return 0
  }
}