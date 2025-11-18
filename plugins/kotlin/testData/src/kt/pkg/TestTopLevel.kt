package pkg

fun topLevelFun() {
    val x = 1
}

var topLevelVar = 42

val topLevelVal = Regex("")

const val topLevelConstVal = 926

fun interpolateTopLevel() {
    println("topLevelVar = $topLevelVar")
    println("topLevelConstVal = $topLevelConstVal")
}
