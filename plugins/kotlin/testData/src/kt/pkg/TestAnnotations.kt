package pkg

class TestAnnotations {
  annotation class TestAnnotation(
    val first: String,
    val second: Int,
  )

  @Repeatable
  annotation class RepeatableAnnotation(
    val value: String,
  )

  @TestAnnotation("test", 1)
  fun test() {
  }

  @RepeatableAnnotation("test")
  @RepeatableAnnotation("test2")
  fun test2() {
  }
}