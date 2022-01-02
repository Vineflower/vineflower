package pkg

enum class TestKotlinEnumWhen {
  FIRST, SECOND, THIRD;

  fun testStatement() {
    when (this) {
      FIRST -> println("first!")
      SECOND -> println("second!")
      THIRD -> println("third!")
    }
  }

  fun testExpression() {
    println(
      when (this) {
        FIRST -> "first!"
        SECOND -> "second!"
        THIRD -> "third!"
      }
    )
  }

  fun testAnotherEnum() {
    // Using a nested function prevents kotlinc from inlining this into "throw NotImplementedError()"
    fun getLevel(): DeprecationLevel = TODO()

    when (val level = getLevel()) {
      DeprecationLevel.WARNING -> println("warning $level")
      DeprecationLevel.ERROR -> println("error $level")
      DeprecationLevel.HIDDEN -> println("hidden $level")
    }
  }
}
