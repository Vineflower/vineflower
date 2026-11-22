package pkg

import java.util.*
import java.util.function.Supplier

class TestTryReturn {
    fun test(supplier: Supplier<Boolean>): Boolean {
        try {
            return supplier.get()
        } catch (var3: Exception) {
            throw RuntimeException(var3)
        }
    }

    fun testFinally(supplier: Supplier<Boolean>): Boolean {
        try {
            return supplier.get()
        } finally {
            println("Finally")
        }
    }

    fun testFinally1(supplier: Supplier<Boolean>) {
        println("pred")

        try {
            if (supplier.get()) {
                return
            }
        } finally {
            println("Finally")
        }

        println("suc")
    }

    fun testFinally2(supplier: Supplier<Boolean>): Boolean {
        var b: Boolean
        try {
            b = supplier.get()
        } finally {
            println("Finally")
        }

        return b
    }

    fun testFinally3(b: Boolean, c: Boolean, a: Int, supplier: Supplier<Boolean>): Boolean {
        try {
            if (b) {
                return c && supplier.get()
            }

            if (a > 0) {
                return a == 1
            }

            return supplier.get()
        } finally {
            println("Finally")
        }
    }

    fun testFinally4(supplier: Supplier<Boolean>): Boolean {
        var b = false
        try {
            b = supplier.get()
        } finally {
            println("Finally")
        }

        return b
    }

    fun testFinally5(supplier: Supplier<Boolean>): Boolean {
        var b = false
        try {
            b = supplier.get()
        } catch (e: Exception) {
            println("Catch")
            b = supplier.get()
        } finally {
            println("Finally")
        }

        return b
    }

    fun testFinally6(a: Boolean, supplier: Supplier<Boolean>): Boolean {
        var b = false
        try {
            if (a) {
                b = true
                println("If")
            }

            b = supplier.get()
        } catch (e: Exception) {
            println("Catch")
            b = supplier.get()
        } finally {
            println("Finally")
        }

        return b
    }

    fun testLoopFinally() {
        val a = true

        while (true) {
            try {
                if (a) {
                    return
                }
            } finally {
                println("Finally")
            }
        }
    }

    fun testParsingFailure() {
        val var1 = 't'
        try {
            if (var1 != 'q') {
                try {
                    println(var1)
                } catch (var6: Exception) {
                    return
                } finally {
                    return
                }
            }
        } finally {
            println(var1)
            return
        }
    }

    fun testParsingFailureSimple() {
        try {
            try {
                println("inner")
            } finally {
                return
            }
        } finally {
            println("fin")
        }
    }

    fun testPostdomFailure() {
        // Load bearing useless string- removing this makes vf emit a parsing error???
        var var1: String?
        println(1)
        label@ while (Random().nextBoolean()) {
            try {
                try {
                    println(2)
                } catch (var9: Exception) {
                    println(3)
                    return
                } finally {
                    continue@label
                }
            } finally {
                val var10: Byte = 28
            }
        }
    }

    fun testVarWrong() {
        var var1: Int
        try {
            println("Hi")
        } catch (var2: Exception) {
            if (var2 != null) {
                return
            } else {
                println(var2 as Exception)  // undo smart cast
                return
            }
        } finally {
            val var3 = 9.18f
        }
    }

    fun testInvalidUse() {
        val var1 = false
        val var3 = "Hi!"
        try {
            println(var1)
            return
        } catch (var4: Exception) {
            try {
                println(var4)
            } catch (var5: Exception) {
                return
            } finally {
                // Unable to correctly guess this is var4
                println(var4)
            }
        } finally {
            // The finally here causes the issue
            println(var3)
        }
    }

    fun returnInCatch() {
        try {
            println("Hi!")
        } catch (e: Exception) {
            println("hello")
            return
        } finally {
            println("finally")
        }

        println("post")
    }
}
