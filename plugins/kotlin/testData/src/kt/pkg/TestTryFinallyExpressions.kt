package pkg

import java.io.IOException

class TestTryFinallyExpressions {
  fun test0(s: String) {
    print(try {
      s.repeat(5)
    } finally {
    	println("bye")
    })
  }

  fun test1(a: String, b: String) {
    var x = a
    test0(try{
      x.repeat(5)
    } finally {
    	x = b
    } + try {
      x.repeat(5)
    } finally {
      println(a)
    } )
  }

  fun test2(a: String, b: String) {
    var x = a
    test1(try{
      if (a == b) {
        return
      }
      x = b
      test0(x)
      x
    } catch (e: IOException) {
      x = a
      test1(a, x)
      x
    } finally {
    	x = if (x == a) {
        b
      } else {
        a
      }
    }, try {
      x.repeat(5).also{x = it}
    } finally {
    	println(x)
    })
  }
}