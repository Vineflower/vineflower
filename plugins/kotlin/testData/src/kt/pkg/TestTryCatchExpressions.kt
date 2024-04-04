package pkg

import java.io.IOException

class TestTryCatchExpressions {
  fun test0(s: String) {
    print(
      try {
        s.repeat(5)
      } catch (e: RuntimeException) {
        e.message ?: "ERROR"
      }
    )
  }

  fun test1(a: String, b: String) {
    var x = a
    test0(
      try {
        x.repeat(5)
      } catch (e: RuntimeException) {
        x = b
        e.message ?: "ERROR"
      } + try {
        x.repeat(5)
      } catch (e: RuntimeException) {
        e.message ?: "ERROR"
      }
    )
  }

  fun test2(a: String, b: String) {
    var x = a
    test1(try {
      if (a.length != b.length) {
        return
      }
      x = b
      test0(x)
      x
    } catch (e: IOException) {
      x = a
      test1(a, x)
      x
    } catch (e: RuntimeException) {
      x = if (x == a) {
        b
      } else {
        a
      }
      x
    }, try {
      x.repeat(5).also { x = it }
    } catch (e: RuntimeException) {
      println(x)
      x + "!!" + (e.message ?: "")
    })
  }
}