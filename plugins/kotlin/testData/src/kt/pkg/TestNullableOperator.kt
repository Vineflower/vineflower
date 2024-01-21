package pkg

class TestNullableOperator {
  fun test(x: Int?): Int {
    return x ?: 0
  }

  fun test2(x: String?): String {
    return x ?: "default"
  }

  fun test2_1(x: Any?): Any {
    return x ?: "default"
  }

  fun test2_2(x: Any?): Any {
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

  fun test6(x: Int?): Int {
    val y = x ?: return 0

    println(y)

    return y
  }

  fun test6_1(x: Int?) {
    val y = x ?: return

    println(y)
  }
}