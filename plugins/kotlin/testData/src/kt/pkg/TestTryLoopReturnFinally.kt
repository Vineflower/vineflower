package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.*

class TestTryLoopReturnFinally {
    private val field = false

    fun test(file: File?) {
        try {
            while (this.field) {
                if (file == null) {
                    return
                }

                val scanner = Scanner(file)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            println("Finally")
        }
    }
}
