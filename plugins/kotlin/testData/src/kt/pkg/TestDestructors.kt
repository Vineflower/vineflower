package pkg

class TestDestructors {
  fun destructDataClasses(
    x: Pair<String, Int?>,
    y: Triple<Number, Boolean?, String>
  ) {
    val (a, b) = x
    println("$a $b")

    val (c, d, e) = y
    println("$c $d $e")
  }

  fun destructDataClassesSpecial(
    x: Pair<Int, String>,
    y: Triple<List<Int>, Nothing?, Unit>
    ) {
    val (a, b) = x
    println("$a $b")

    val (c, d, e) = y
    println("$c $d $e")
  }

  fun destructDataClassesSkip(
    x: Triple<String, Int?, String>,
    y: Triple<Number, Boolean?, String>
  ) {
    val (_, a) = x
    println(a)

    val (d, _, e) = y
    println("$d $e")
  }

  fun destructorImpossible(x: Pair<String, Nothing>) : String {
    val (a, b) = x
    // https://youtrack.jetbrains.com/issue/KT-12604/No-Unreachable-code-with-componentN-function-returning-Nothing
    return a
  }

  fun destructExtensionFunction(x: Int) {
    val (a, b, c) = x
    println("$a$b$c")
  }

  inline fun destructInlineLambda(x: () -> Int) {
    val (a, b, c) = x
    println("$a$b$c")
  }

  fun callDestructInlineLambda() {
    destructInlineLambda { 123 }
  }

  fun callDestructInlineLambdaWithControlFlow(x: Int) {
    destructInlineLambda { if (x in 100..999) x else return }
  }

  fun destructInlineLambdaNoInline(x: () -> Int) {
    val (a, b, c) = x
    println("$a$b$c")
  }

  fun destructLambdaInline(x: Int) {
    val (a, b, c) = { x }
    println("$a$b$c")
  }
//
//  fun destructLambdaInlineControlFlow(x: Int) {
//    val (a, b, c) = { if (x in 100..999) x else return }
//    println("$a$b$c")
//  }


  operator fun Int.component1() = this.toString()[0] - '0'
  operator fun Int.component2() = this.toString()[1] - '0'
  operator fun Int.component3() = this.toString()[2] - '0'

  inline operator fun (() -> Int).component1() = this().component1()
  inline operator fun (() -> Int).component2() = this().component2()
  inline operator fun (() -> Int).component3() = this().component3()
}