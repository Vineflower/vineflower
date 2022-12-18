package pkg

object TestObject {
  fun objectFun() {
    objectVar--
  }

  private var objectVar = 42

  val objectVal = Regex("")

  const val objectConstVal = 926

  @JvmStatic
  fun objectJvmStaticFun() {
    objectVar++
  }
}