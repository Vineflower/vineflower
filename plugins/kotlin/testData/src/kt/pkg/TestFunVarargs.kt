package pkg

class TestFunVarargs {
  fun printAll(vararg messages: String) {
    for (m in messages) println(m)
  }

  fun printAllArray(messages: Array<out String>) {
    for (m in messages) println(m)
  }

  fun log(vararg entries: String) {
    printAll(*entries)
    printAllArray(entries)
  }

  fun test() {
    log("a", "b", "c")
  }
}