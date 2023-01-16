package pkg

class TestComparison {
  fun test2(a: Any, b: Any): Boolean {
    return a == b
  }

  fun test3(a: Any, b: Any): Boolean {
    return a === b
  }

  fun testNull2(a: Any?): Boolean {
    return a == null
  }

  fun testNull3(a: Any?): Boolean {
    return a === null
  }

  fun testNullDouble2(a: Any?, b: Any?): Boolean {
    return a == b
  }

  fun testNullDouble3(a: Any?, b: Any?): Boolean {
    return a === b
  }
}