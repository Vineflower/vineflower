package pkg

class TestTryCatchNested {
    fun test() {
        val var1 = 20f
        try {
            try {
                println(var1)
                return
            } catch (var7: Exception) {
            }
        } catch (var10: Exception) {
            println(var1)
        }
    }
}
