package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.*

class TestTryLoopRecompile {
    private val field = false

    fun test(file: File) {
        while (true) {
            try {
                if (this.field) {
                    Scanner(file)
                    continue
                }
            } catch (var3: FileNotFoundException) {
                var3.printStackTrace()
            }

            return
        }
    }
}
