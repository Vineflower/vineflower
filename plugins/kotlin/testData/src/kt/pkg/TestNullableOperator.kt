package pkg

class TestNullableOperator {
  fun test(x: Int?): Int {
    return x ?: 0
  }

  fun test2(x: String?): String {
    return x ?: "default"
  }

  fun test3(x: Int?): Int {
    return x ?: throw Exception()
  }

  fun test4(x: Exception?) {
    x?.printStackTrace()
  }

  fun test5(x: Exception?) {
    x?.printStackTrace() ?: throw Exception()
  }
}