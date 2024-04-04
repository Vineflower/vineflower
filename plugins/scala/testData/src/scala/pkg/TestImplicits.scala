package pkg

trait Monoid[T] {
  def op(x: T, y: T): T
  def id: T
}

object AddMonoid extends Monoid[Int] {
  override def op(x: Int, y: Int): Int = x + y
  override def id: Int = 0
}

object MulMonoid extends Monoid[Int]{
  override def op(x: Int, y: Int): Int = x * y
  override def id: Int = 1
}

object ConcatMonoid extends Monoid[String] {
  override def op(x: String, y: String): String = x ++ y
  override def id: String = ""
}

class TestImplicits {
  implicit def am: AddMonoid.type = AddMonoid // not Mul
  implicit def cm: ConcatMonoid.type = ConcatMonoid

  def use[T](x: T)(implicit monoid: Monoid[T]): T = {
    monoid.op(x, x)
  }

  def user(): Unit = {
    print(use(3))
    print(use("hia"))
  }
}