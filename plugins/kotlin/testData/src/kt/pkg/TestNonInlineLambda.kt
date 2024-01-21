package pkg

@Suppress("RedundantVisibilityModifier")
open class TestNonInlineLambda {

  fun testCaptureInt(x : Int) {
    val y = x
    execute {
      println(y)
    }
  }

  fun testCaptureObject(x: String) {
    val y = x
    execute {
      println(y)
    }
  }

  fun testCaptureIntIterationValue(x: Iterable<Int>) {
    for (i in x){
      execute {
        println(i)
      }
    }
  }

  fun testCaptureObjectIterationValue(x : Iterable<String>) {
    for (i in x){
      execute {
        println(i)
      }
    }
  }

  fun testCaptureMutableInt(x: Int){
    var y = x
    execute {
      println(y)
    }
    y++
    execute {
      println(y)
    }
    y *= 500
    execute {
      println(y)
    }
    y = 100
    execute {
      println(y)
    }
    y += x
    execute {
      println(y)
    }
  }

  fun testCaptureMutableObject(x: String){
    var y = x
    execute {
      println(y)
    }
    y += "!!"
    execute {
      println(y)
    }
    y += y + y
    execute {
      println(y)
    }
    y = "Hello: "
    execute {
      println(y)
    }
    y += x
    execute {
      println(y)
    }
  }

  fun testCaptureAndMutateInt(x: Int){
    var y = 0
    execute {
      while (y < 10) {
        println(y++)
      }
    }
    y = 5 + x
    execute {
      while (y > 0) {
        println(y--)
      }
    }
  }

  fun testCaptureAndMutateString(x: String){
    var y = ""
    execute {
      while (y.length < 10) {
        y = " " + y
        println(y)
      }
    }
    y = "Hello: " + x
    execute {
      while (y.isNotBlank()) {
        println()
        y = y.drop(1)
      }
    }
  }

  public var intField = 0

  fun testCapturePublicMutableIntField() {
    execute { intField++ }
  }

  public var stringField = ""

  fun testCapturePublicMutableStringField() {
    execute { stringField += "!" }
  }

  private var privateIntField = 0

  fun testCapturePrivateMutableIntField() {
    execute { privateIntField++ }
  }

  private var privateStringField = ""

  fun testCapturePrivateMutableStringField() {
    execute { privateStringField += "!" }
  }


  open fun execute(block: () -> Unit) {

  }
}