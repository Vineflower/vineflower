package pkg

class TestTailrecFunctions {
  tailrec fun sum(x: Long, sum: Long): Long {
    if (x == 0.toLong()) return sum
    return sum(x - 1, sum + x)
  }

  tailrec fun testFinally() {
    try {
      // do nothing
    } finally {
      testFinally()
    }
  }

  tailrec fun testFinallyReturn() : Int {
    try {
      // do nothing
    } finally {
      return testFinallyReturn()
    }
  }

  tailrec fun fooTry() {
    try {
      return fooTry()
    }
    catch (e: Throwable) {
    }
  }

  tailrec fun testTryCatchFinally() : Unit {
    try {
      testTryCatchFinally()
    } catch (any : Exception) {
      testTryCatchFinally()
    } finally {
      testTryCatchFinally()
    }
  }

  tailrec fun fastPow(x: Long, n: Long, acc: Long = 1L): Long {
    if (n == 0L) return acc
    if (n % 2 == 0L) return fastPow(x * x, n / 2, acc)
    return fastPow(x, n - 1, acc * x)
  }

  tailrec fun fastPow(x: Long, n: Long) : Long = when{
    n == 0L -> 1L
    n % 2 == 0L -> fastPow(x * x, n / 2)
    else -> x * fastPow(x, n - 1)
  }


}