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
    // Using a nested function prevents kotlinc from inlining the exception
    fun getLevel(): DeprecationLevel = throw Exception()

    when (val level = getLevel()) {
      DeprecationLevel.WARNING -> println("warning $level")
      DeprecationLevel.ERROR -> println("error $level")
      DeprecationLevel.HIDDEN -> println("hidden $level")
    }
  }

  fun testConsecutive() {
    when (this) {
      FIRST -> println("first!")
      SECOND -> println("second!")
      THIRD -> println("third!")
    }

    when (this) {
      FIRST -> println("first, again!")
      SECOND -> println("second, again!")
      THIRD -> println("third, again!")
    }
  }
}
