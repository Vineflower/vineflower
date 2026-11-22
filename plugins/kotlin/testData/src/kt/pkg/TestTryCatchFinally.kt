/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pkg

class TestTryCatchFinally {
    fun test1(x: String?) {
        try {
            println("sout1")
        } catch (e: Exception) {
            try {
                println("sout2")
            } catch (e2: Exception) {
                // Empty
                // Empty
                // Empty
            }
        } finally {
            println("finally")
        }
    }

    @Throws(Exception::class)
    fun foo(a: Int): Int {
        if (a < 1) {
            throw RuntimeException()
        } else if (a < 5) {
            return a
        } else {
            throw Exception()
        }
    }

    fun test(a: String): Int {
        try {
            return a.toInt()
        } catch (e: Exception) {
            println("Error" + e)
        } finally {
            println("Finally")
        }
        return -1
    }

    fun testEmptyCatch(msg: String?) {
        try {
            println(msg)
        } catch (ignored: RuntimeException) {
        } finally {
            println("Bye")
        }
    }
}