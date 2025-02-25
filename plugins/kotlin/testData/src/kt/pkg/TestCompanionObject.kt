package pkg

class TestCompanionObject {
  fun notInCompanion() {
    println("notInCompanion")
  }

  companion object {
    val companionVal = "inCompanion"

    @JvmStatic
    val companionValJvmStatic = "inCompanionJvmStatic"

    fun inCompanion() {
      println("inCompanion")
    }

    @JvmStatic
    fun inCompanionJvmStatic() {
      println("inCompanionJvmStatic")
    }
  }
}