package pkg

import pkg.Constants.LN2
import pkg.Constants.taylor_2_bound
import pkg.Constants.taylor_n_bound
import pkg.Constants.upper_taylor_2_bound
import pkg.Constants.upper_taylor_n_bound


private object Constants {
  val LN2: Double = Math.log(2.0)
  val epsilon: Double = Math.ulp(1.0)
  val taylor_2_bound = Math.sqrt(epsilon)
  val taylor_n_bound = Math.sqrt(taylor_2_bound)
  val upper_taylor_2_bound = 1 / taylor_2_bound
  val upper_taylor_n_bound = 1 / taylor_n_bound
}

fun asinh(x: Double): Double =
  when {
    x >= +taylor_n_bound ->
      if (x > upper_taylor_n_bound) {
        if (x > upper_taylor_2_bound) {
          // approximation by laurent series in 1/x at 0+ order from -1 to 0
          Math.log(x) + LN2
        } else {
          // approximation by laurent series in 1/x at 0+ order from -1 to 1
          Math.log(x * 2 + (1 / (x * 2)))
        }
      } else {
        Math.log(x + Math.sqrt(x * x + 1))
      }
    x <= -taylor_n_bound -> -asinh(-x)
    else -> {
      // approximation by taylor series in x at 0 up to order 2
      var result = x;
      if (Math.abs(x) >= taylor_2_bound) {
        // approximation by taylor series in x at 0 up to order 4
        result -= (x * x * x) / 6
      }
      result
    }
  }