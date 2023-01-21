package pkg

fun accept(f: (Int) -> String) = println(f.invoke(5))

fun function(r: Int): String {
    return "OK".repeat(r)
}

fun test() {
  accept(::function)
}