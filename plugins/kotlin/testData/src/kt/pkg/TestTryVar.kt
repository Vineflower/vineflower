package pkg

class TestTryVar {
    fun test(x: Int, y: Int): Int {
        var x = x
        for (i in 0..9) {
            try {
                x = y + i
                x = 5000 - i
                x = y / y
            } catch (t: Throwable) {
            }
        }

        return x
    }

    fun test2(x: Int, y: Int): Int {
        var x = x
        for (i in 0..9) {
            try {
                x = y + i
                x = (5000 - i / (7 - i).let { x += it; x }) / y
            } catch (t: Throwable) {
            }
        }

        return x
    }
}
