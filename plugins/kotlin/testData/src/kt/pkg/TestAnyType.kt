package pkg

class TestAnyType {
  fun test(param: Any): Int {
    if (param is String) {
      return param.length
    }

    println(param)

    return 0
  }
}