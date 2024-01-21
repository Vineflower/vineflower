package pkg

class TestCompileTimeErrors {
  interface Test {
    val testValue: Int
  }

  @Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")
  fun <I, O> test(i: I): O where O : I, O : Test {
    TODO()
  }

  fun test2(i: Int?): Test? {
    return if (i == null) null else object : Test {
      @Suppress("PROPERTY_TYPE_MISMATCH_ON_OVERRIDE")
      override val testValue = i
    }
  }
}
