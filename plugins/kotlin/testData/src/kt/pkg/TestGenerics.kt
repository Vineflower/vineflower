package pkg

class TestGenerics<T> {
  fun <T> genericFun(v: T): T {
    return v
  }

  fun <T> nullableGeneric(v: T): T? {
    return null
  }

  fun <T> subType(v: TestGenerics<out T>) {
  }

  fun <T> superType(v: TestGenerics<in T>) {
  }

  fun any(v: TestGenerics<*>) {
  }
}