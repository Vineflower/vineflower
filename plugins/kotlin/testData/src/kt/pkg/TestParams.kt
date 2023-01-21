package pkg

class TestParams {
  fun printMessageUnit(message: String): Unit {
    println(message)
  }

  fun printMessageVoid(message: String) {
    println(message)
  }

  fun multiply(x: Int, y: Int) = x * y

  fun multiplyBraces(x: Int, y: Int): Int {
    return x * y
  }

  fun printMessageWithPrefix(message: String, prefix: String = "Info") {
    println("[$prefix] $message")
  }

  fun callPrintMessage() {
    printMessageWithPrefix("Test")
    printMessageWithPrefix("Test", "Debug")
  }
}