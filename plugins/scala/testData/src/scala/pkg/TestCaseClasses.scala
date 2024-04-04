package pkg

sealed trait TestCaseClasses

case class Option1(value: Int) extends TestCaseClasses
case class Option2(x: Double, y: Double, z: Double) extends TestCaseClasses
case class Option3(value: List[String]) extends TestCaseClasses
case object EnumLike extends TestCaseClasses