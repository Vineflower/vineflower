package pkg

trait Shuffler {
  type T
  def shuffle(x: T): T
}

object IntShuffler extends Shuffler {
  override type T = Int
  override def shuffle(x: Int): Int = x + 1
}

object StringShuffler extends Shuffler {
  override type T = String
  override def shuffle(x: String): String = x.reverse
}

class TestAssocTypes {
  def user(): Unit = {
    print(IntShuffler.shuffle(3))
    print(StringShuffler.shuffle("abcd"))
  }
}