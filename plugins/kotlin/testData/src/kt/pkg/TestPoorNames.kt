package pkg

class TestPoorNames {
  fun `Function with spaces`() {
  }

  fun `Dangerous function name?!`() {
  }

  val `Property with spaces` = 42
  val `Dangerous property name?!` = "test"

  fun `functionWith$Dollar`() {
  }

  fun functionWithParameters(`Parameter with spaces`: Int, `Dangerous parameter name?!`: String) {
  }

  class `Class with spaces`

  fun test() {
    val instance = `Class with spaces`()
    `Dangerous function name?!`()
    functionWithParameters(`Parameter with spaces` = 42, `Dangerous parameter name?!` = "test")
  }
}
