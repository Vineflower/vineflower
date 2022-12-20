package pkg

class TestSynchronized {
  fun test() {
    synchronized (this) {
      println("Hello")
    }
  }
}