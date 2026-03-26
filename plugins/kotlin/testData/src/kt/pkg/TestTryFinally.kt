package pkg

class TestTryFinally {
    fun test0() {
        try {
            println("Hello")
        } finally {
            val l: Long = 5
        }
    }

    fun test1() {
        try {
            println("Hello")
        } finally {
            println("Finally")
        }

        println("Bye")
    }


    fun test2(i: Int) {
        try {
            println("Hello")
        } finally {
            println("Finally")
            if (i > 0) {
                println(i)
                return
            }
        }

        println("Bye")
    }
}
