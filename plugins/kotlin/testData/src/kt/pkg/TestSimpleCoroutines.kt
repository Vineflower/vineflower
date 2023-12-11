package pkg

import java.util.Dictionary

class TestSimpleCoroutines {

  suspend fun testA(a: Int): Int {
    return 0
  }

  suspend fun testSingleWait(a: Int): Int {
    return a + testA(a)
  }

  suspend fun testSingleWaitAtTail(a: Int): Int {
    return testA(a + 5)
  }

  suspend fun testDoubleWait(a: Int): Int {
    return a + testA(a) + testSingleWait(a + 1)
  }

  suspend fun testWithWaitAtEnd(a: Int): Int {
    return testSingleWaitAtTail(testA(a) + testSingleWait(a + 1))
  }

  suspend fun testConditionalWait(a: Int): Int {
    if (a > 0) {
      return a + testA(a)
    }

    return a
  }

  suspend fun testConditionalWaitPhi(a: Int): Int {
    var x = 0
    if (a > 0) {
      x = a + testA(a)
    } else {
      x = a + 3
    }

    return x
  }

  suspend fun testConditionalWaitAtTail(a: Int): Int {
    if (a > 0) {
      return testA(a + 5)
    }

    return a
  }

  suspend fun testMultipleConditionalWait(a: Int): Int {
    if (a > 0) {
      return a + testA(a)
    }

    if (a < 10) {
      return a + testA(a + 9)
    }

    return a
  }

  suspend fun testSuspendInCondition(a: Int): Boolean {
    if (testA(a) == 0) {
      println("testA is 0")
      return true
    }

    return false
  }

  suspend fun testSuspendedBooleanInCondition(a: Int): Boolean {
    if (testSuspendInCondition(a)) {
      println("testSuspendInCondition is true")
      return true
    }

    return false
  }

  suspend fun testReturnNullability(a: Int): Dictionary<String?, Dictionary<List<Int?>, List<Int>>?> {
    TODO()
  }
}