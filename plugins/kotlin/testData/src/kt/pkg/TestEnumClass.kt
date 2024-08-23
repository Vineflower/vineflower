package pkg

enum class TestEnumClass(val number: Int = 4) {
  A, B, C(3), D(5)
  ;

  fun foo() = number
}