package pkg

import java.util.function.Consumer

class TestAnonymousEverything {
  fun testPlainLambda() {
    val lambda: (Int, Int) -> Int = { x, y -> x + y }
    lambda(1, 2)
  }

  fun testSerializableLambda() {
    val lambda: (Int, Int) -> Int = @JvmSerializableLambda { x, y -> x + y }
    lambda(1, 2)
  }
  
  fun testAnonymousObject() {
    val obj = object : Any() {
      fun foo(x: Int, y: Int): Int {
        return x + y
      }
    }
    obj.foo(1, 2)
  }
  
  fun testFunctionAnonymousObject() {
    val obj = object : (Int, Int) -> Int {
      override fun invoke(x: Int, y: Int): Int {
        return x + y
      }
    }
    obj(1, 2)
  }

  fun testAnonymousFunction() {
    val func = fun(x: Int, y: Int) = x + y
    func(1, 2)
  }

  fun add(x: Int, y: Int) = x + y

  fun testFunctionReference() {
    val ref = ::add
    ref(1, 2)
  }

  fun testConstructorReference() {
    val createAny = ::Any
    createAny()
  }

  fun testNoTypeReference() {
    val newList: (Any) -> List<*> = ::listOf
    newList(1)
  }

  fun testMultipleSubtypes() {
    val obj = object : Runnable, Consumer<Unit> {
      override fun run() = println("Runnable.run")
      override fun accept(t: Unit) = println("Consumer.accept")
    }

    obj.run()
    obj.accept(Unit)
  }

  fun testNestedLambdas() {
    val lambda1: (Int) -> Int = { x ->
      val lambda2: (Int) -> Int = { y -> x + y }
      lambda2(1)
    }
    lambda1(1)
  }

  fun testCapturedLocalVariables() {
    var x = 1
    val mutating: () -> Int = { x++ }
    mutating()

    val y = 1
    val nonMutating: () -> Int = { y }
    nonMutating()
  }
  
  fun testEarlyReturn() {
    val lambda: (Int) -> Int = lambda@ { x ->
      if (x == 0) {
        return@lambda 42
      }

      if (x == Int.MIN_VALUE) {
        return@lambda Int.MAX_VALUE
      }

      if (x < 0) {
        return@lambda 0
      }

      x + 1
    }
    lambda(1)
    lambda(-1)
  }

  fun testThrows() {
    val lambda: (Int) -> Int = { x ->
      if (x < 0) {
        throw IllegalArgumentException("x must be >= 0")
      }

      require(x != Int.MAX_VALUE) { "x must not be Int.MAX_VALUE" }

      x + 1
    }
  }
}