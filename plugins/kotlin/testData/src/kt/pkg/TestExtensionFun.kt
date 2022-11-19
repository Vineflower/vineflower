package pkg

class TestExtensionFun {
  fun CharSequence.repeat2(n: Int): String {
    return this.repeat(n);
  }

  fun test() {
    println("Bye ".repeat2(2))
  }
}