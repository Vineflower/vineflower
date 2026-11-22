package pkg

import java.io.FileInputStream
import java.io.InputStream

class TestTrySynchronized {

    @Throws(Exception::class)
    fun case01() {
        synchronized(monitor) {
            println("Inside synchronized block.")
        }

        var stream: InputStream? = null
        try {
            stream = FileInputStream("nul")
        } catch (e: Throwable) {
            stream!!.close()
        }
    }

    @Throws(Exception::class)
    fun case02() {
        synchronized(monitor) {
            println("Inside first synchronized block.")
        }

        var stream: InputStream? = null
        try {
            stream = inputStream

            synchronized(monitor) {
                println("Inside second synchronized block.")
            }
        } catch (e: Throwable) {
            stream!!.close()
        }
    }

    private val inputStream: InputStream?
        get() = null

  companion object{
    private val monitor = TestTrySynchronized()
  }
}
