package pkg

sealed class TestSealedHierarchy {
  object TestObject : TestSealedHierarchy()
  class TestClass(val x: Int) : TestSealedHierarchy()
}
