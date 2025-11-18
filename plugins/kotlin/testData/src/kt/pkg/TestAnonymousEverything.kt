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
}