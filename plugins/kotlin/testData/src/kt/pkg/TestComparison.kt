package pkg

class TestComparison {
  fun test2(a: Any, b: Any): Boolean {
    return a == b
  }

  fun test3(a: Any, b: Any): Boolean {
    return a === b
  }
}