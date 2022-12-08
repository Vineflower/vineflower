package pkg

class TestGenerics {
  fun <T> genericFun(v: T): T {
    return v
  }

  fun <T> nullableGeneric(v: T): T? {
    return null
  }
}