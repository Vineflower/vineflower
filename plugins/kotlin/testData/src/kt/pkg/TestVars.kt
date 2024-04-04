package pkg

class TestVars {
  fun testVar() {
    var a: String = "initial"
    println(a)
    var b: Int = 1
    var c = 3
  }

  fun testVal() {
    val a: String = "initial"
    println(a)
    val b: Int = 1
    val c = 3
  }

  fun testPhi(bl: Boolean) {
    val d: Int

    if (bl) {
      d = 1
    } else {
      d = 2
    }

    println(d)
  }

  fun testIfExpr(bl: Boolean) {
    val d: Int = if (bl) {
      1
    } else {
      2
    }

    println(d)
  }
}