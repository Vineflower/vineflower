package pkg

class TestLabeledJumps {
  fun testContinue(tester: (Int) -> Boolean) {
    loop@ for (i in 1..100) {
      for (j in 1..100) {
        if (tester.invoke(j)) {
          continue@loop
        }

        println("$j $i")
      }

      println("loop")
    }
  }

  fun testBreak(tester: (Int) -> Boolean) {
    loop@ for (i in 1..100) {
      for (j in 1..100) {
        if (tester.invoke(j)) {
          break@loop
        }

        println("$j $i")
      }
    }

    println("end")
  }
}