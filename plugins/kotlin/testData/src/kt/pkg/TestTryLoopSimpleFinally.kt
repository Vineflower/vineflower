package pkg

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import java.util.*

class TestTryLoopSimpleFinally {
    private val field = false

    fun test(file: File) {
        try {
            while (this.field) {
                val scanner = Scanner(file)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            println("Finally")
        }
    }

    @Throws(IOException::class)
    fun test2(x: Int, file: Path) {
        var x = x
        try {
            while (x >= 0) {
                val scanner = Scanner(file)

                if (x % 11 == 0) {
                    println("nice")
                    return
                }

                x -= scanner.nextInt()
            }
        } finally {
            println("Finally")
        }
    }
}
