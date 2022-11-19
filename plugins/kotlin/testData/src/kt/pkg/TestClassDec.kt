package pkg

class TestClassDec {
  class EmptyDec

  class Vec2iVar(var x: Int, var y: Int)
  class Vec2iVal(val x: Int, val y: Int)
  class Vec2i(x: Int, y: Int)

  fun Vec2iVal.dot(v: Vec2iVal): Int {
    return x * v.x + y * v.y
  }

  fun test() {
    var a = EmptyDec()
    var vec = Vec2iVal(1, 2)
    var vec1 = Vec2iVal(2, 4)

    println(vec.x)
    println(vec.dot(vec1))
  }
}