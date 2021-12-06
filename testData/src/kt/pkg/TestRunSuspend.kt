package pkg

// Code used from Kotlin's stdlib RunSuspend.kt
private class TestRunSuspend {
  var result: Result<Unit>? = null

  fun await() = synchronized(this) {
    while (true) {
      when (val result = this.result) {
        null -> @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") (this as Object).wait()
        else -> {
          result.getOrThrow() // throw up failure
          return
        }
      }
    }
  }
}
