package pkg

class TestSmartCasts {
  interface X  {
    fun Iterable<*>.woo() = "A"
  }
  sealed class A {
    class B : A() {
      fun testB(): String = "B"
    }
    class C : A() {
      fun testC(): String = "C"
    }

    fun test(): String = ""
  }

  fun testWhen(o: Any?): String {
    when (o) {
      is String -> {
        return o
      }

      is A.B -> println("B: $o")
      is A.C -> println("C: $o")
      is Pair<*, *> -> return "<${testWhen(o.first)}, ${testWhen(o.second)}>"
      null -> return "null"
      else -> return "else: $o"
    }

    return (o as A).test()
  }

  fun testIf(a: Any?): String {
    if (a is A.B || a is A.C) {
      return (a as A).test()
    }

    return "else: $a"
  }

  fun testIf2(a: Any?): String {
    if (a is A) {
      if (a is A.B || a is A.C) {
        println(a.test())
      }

      if (a is A.B) {
        if (a is A.C) {
          println(a.testB())
          println(a.testC())
        }

        if (a is A.C && a.testC() == "C" || a is A.B) {
          println(a.testB())
        }
      }
    }

    return "else: $a"
  }

  fun testCast(a: Any?) {
    println(a)
    a as String
    println("hello")
    println(a)
    a[0]
    println(a[0])
  }

  fun testSealedIf(a: A): String {
    if (a is A.B) {
      return a.testB()
    } else if (a is A.C) {
      return a.testC()
    } else {
      return a.test()
    }
  }

  fun testDoubleType(t: List<String>) : String {
    if (t is X) with(t){
      return woo()
    }

    return t[0]
  }
}