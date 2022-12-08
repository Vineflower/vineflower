package pkg

class TestConvertedK2JOps {
  val list: List<String> = listOf("a", "b", "c")
  val set: Set<String> = setOf("a", "b", "c")
  val map: Map<String, String> = mapOf("a" to "b", "c" to "d")
  val any: Any = Any()

  fun codeConstructs() {
    println("Hello, world!")
    val concatenations = "a" + "b" + "c"
  }
}