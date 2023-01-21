package pkg

class TestNullable {
  fun nullableParams(v: String, vn: String?) {
  
  }
  
  fun nullableReturn(): String? {
    return null
  }

  fun nullableGenerics(v: List<String?>): List<String?>? {
    return v
  }
}