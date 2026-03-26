package pkg

class TestTryCatchThrowable {
    fun testEmptyCatch() {
        try {
            println("Hi")
        } catch (ignored: Throwable) {
        }
    }

    fun testEmptyCatchWithTail() {
        try {
            println("Hi")
        } catch (ignored: Throwable) {
        }

        println("tail")
    }

    fun testCatchWithReturn(): String? {
        try {
            println("Hi")
            return "5"
        } catch (ignored: Throwable) {
            return null
        }
    }

    fun testCatchWithReturnAndTail(): String? {
        try {
            println("Hi")
        } catch (ignored: Throwable) {
            return null
        }
        println("Ho")
        return "bye"
    }

    fun testCatchNested(): String? {
        try {
            println("Hi")
            return "hello"
        } catch (ignored: Throwable) {
            try {
                println("Hi!")
                return "5"
            } catch (throwable: Throwable) {
                return throwable.message
            }
        }
    }

    fun testCatchWithRethrow(): String {
        try {
            println("Hi")
            return "5"
        } catch (throwable: Throwable) {
            println("Oh no")
            throw throwable
        }
    }


    fun testCatchAndOtherEmptyCatch() {
        try {
            println("Hi")
        } catch (ignored: RuntimeException) {
        } catch (ignored: Throwable) {
            println("Oh no")
        }
    }

    fun testCatchAndOtherNonEmptyCatch() {
        try {
            println("Hi")
        } catch (ignored: RuntimeException) {
            println("Hello")
        } catch (ignored: Throwable) {
            println("Oh no")
        }
    }
}

