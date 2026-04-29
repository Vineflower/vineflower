package pkg

class TestTrySplit {
  fun test() {
    var obj: Any? = null
    try {
      obj = Any()
    } catch (ex: ArithmeticException) {
      if (obj != null) {
        println("a")
      }

      throwMyException(ex.message)
      return
    } finally {
      println("b")
    }
  }

  fun testFlat() {
    var obj: Any? = null
    run b1@{
      run b2@{
        try {
          try {
            obj = Any()
          } catch (ex: ArithmeticException) {
            if (obj != null) {
              println("a")
            }

            throwMyException(ex.message)
            return@b2
          }
          return@b1
        } catch (t: Throwable) {
          println("b")
          throw t
        }
      }
      println("b")
      return
    }


    println("b")
  }

  companion object {
    fun throwMyException(message: String?) {
      throw RuntimeException(message)
    }
  }
}
