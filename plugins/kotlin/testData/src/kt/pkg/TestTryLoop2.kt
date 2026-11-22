package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.*

class TestTryLoop2 {
    private val field = false

    fun test(file: File) {
        while (true) {
            try {
                if (this.field) {
                    val scanner = Scanner(file)

                    continue
                }

                break
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}
