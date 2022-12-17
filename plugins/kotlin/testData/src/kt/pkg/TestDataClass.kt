package pkg

data class TestDataClass(
  val dataClassVal: Regex,
  val variableWithVeryLongName: Int,
  val requestLineWrapsIfTheParamListIsTooLong: List<String>,
  val nullability: String?,
)
