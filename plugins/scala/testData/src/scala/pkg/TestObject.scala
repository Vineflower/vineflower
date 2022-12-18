package pkg

object TestObject {
  
  val SOME_STRING = "some string"
  val SOME_INT = 0
  
  def fib(idx: Int): Int =
    if idx == 0 then
      0
    else if idx == 1 then
      1
    else
      fib(idx - 1) + fib(idx - 2)
}
