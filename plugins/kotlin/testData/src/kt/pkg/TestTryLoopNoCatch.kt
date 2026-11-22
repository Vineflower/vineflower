package pkg

class TestTryLoopNoCatch {
    fun test(s: Array<String?>) {
        var b = false
        for (i in s.indices) {
            try {
                b = method(s[i]!!)
                break
            } catch (e: Exception) {
            }
        }

        println(b)
    }

    @Throws(Exception::class)
    private fun method(s: String): Boolean {
        if (s.length > 20) {
            throw Exception()
        }

        return s.length > 10
    }
}
