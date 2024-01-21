package pkg

object TestDefaultParams {

  def defaulted(s: String = "hello!"): Unit = {
    print(s)
  }

  def user(): Unit = {
    defaulted()
    defaulted("world!")
  }
}