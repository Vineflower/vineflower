package pkg;

import java.util.stream.Stream;

public class TestWhileTernary10 {
  public double test(boolean condition, int n, Stream<Double> doubles) {
    double[] ds = new double[]{n};

    for (int i = 0; condition ? i >= n : n >= i; i++) {
      for (int j = 0; j < n; j++) {
        System.out.println(1);

        if (j > i) {
          j++;
        }
      }
    }

    doubles.forEach(d -> ds[0] -= d);
    return ds[0];
  }

  public double test1(boolean condition, int n, Stream<Double> doubles) {
    double[] ds = new double[]{n};

    for (int i = 0; condition ? i >= n : n >= i; i++) {
      ds[0] += i;
    }

    doubles.forEach(d -> ds[0] -= d);
    return ds[0];
  }
}
