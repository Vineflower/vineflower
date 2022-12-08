package pkg

class TestKotlinTypes {
  fun throwAlways(): Nothing {
    throw Exception()
  }

  val consumer: (Int) -> Unit = {}
}