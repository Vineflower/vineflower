package pkg

trait TestCompanionObject {

  def defaultMethod(): Int = 0

  def abstractMethod(): Unit
}

object TestCompanionObject {

  val FIELD = "constant field"

  def main(args: Array[String]): Unit = print(FIELD)
}