package pkg

class TestAnnotations {
  annotation class TestAnnotation(
    val first: String,
    val second: Int,
  )

  @TestAnnotation("test", 1)
  fun test() {
  }
}