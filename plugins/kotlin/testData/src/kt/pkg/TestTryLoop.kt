package pkg

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class TestTryLoop {
    private val field = false

    fun test(file: File) {
        try {
            while (this.field) {
                val scanner = Scanner(file)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    fun hasNext(p: Path, f: MutableIterator<File?>): Boolean {
        var a: File?
        while (true) {
            try {
                if (Files.exists(p)) {
                    a = f.next()
                    return true
                } else return false
            } catch (e: ServiceConfigurationError) {
                println(1)
            } catch (e: NoClassDefFoundError) {
                println(2)
            }
        }
    }
}
