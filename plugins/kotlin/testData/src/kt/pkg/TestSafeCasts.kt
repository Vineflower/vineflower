package pkg

class TestSafeCasts {
  fun test(obj: Any): Boolean {
    val t = obj as? Int

    return t == 1
  }

  fun testTestBefore(obj: Any): Boolean? {
    if (obj !is Int) {
      return null
    }

    val t = obj as? Int

    return t == 1
  }

  fun testHardIncompatible(obj: Int): Boolean {
    val t = obj as? String

    return t == "1"
  }

  fun testSmartCastIncompatible(obj: Any): Boolean {
    if (obj !is Int) {
      return false
    }

    val t = obj as? String

    return t == "1"
  }

  fun testCastNonNullToNullable(obj: Any): Boolean {
    val t = obj as? Int?

    return t == 1
  }

  fun testBeforeNonNullToNullable(obj: Any): Boolean? {
    if (obj !is Int?) {
      return null
    }

    val t = obj as? Int?

    return t == 1
  }

  fun testCastNullableToNullable(obj: Any?): Boolean {
    val t = obj as? Int?

    return t == 1
  }

  fun testBeforeNullableToNullable(obj: Any?): Boolean? {
    if (obj !is Int?) {
      return null
    }

    val t = obj as? Int?

    return t == 1
  }
}