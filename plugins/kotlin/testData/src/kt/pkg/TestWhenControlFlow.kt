package pkg

class TestWhenControlFlow {
  fun test1(x: Int) {
    var x = x

    while (x > 0) {
      x--
      when (x) {
        10 -> break
        5 -> continue
        in 3..4 -> return
      }

      println(x)
    }
  }

}