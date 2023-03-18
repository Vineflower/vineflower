package pkg

class TestReflection {
  fun testClassReference() {
    println(TestReflection::class)
    println(TestReflection::class.java)
  }

  fun testPrimitiveWrapper() {
    println(Int::class)
    println(Int::class.java)
  }

  fun testPrimitiveType() {
    println(Int::class.javaPrimitiveType)
  }

  fun testFunctionReference() {
    val f = TestReflection::testClassReference
    println(f)
    f(TestReflection())
  }
}