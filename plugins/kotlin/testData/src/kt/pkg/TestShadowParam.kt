package pkg

class TestShadowParam {
  fun test(x: Int) {
    var x = x
    x--
    println(x)
    if (x < 0) {
      println(x)
    }
  }
}