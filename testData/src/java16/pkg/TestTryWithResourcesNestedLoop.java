package pkg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestTryWithResourcesNestedLoop {
  public static InputStream test(Path base, Path overlay, int width, int left, int top, int right, int bottom) throws IOException {
    try (InputStream lv = Files.newInputStream(base);
         InputStream lv2 = Files.newInputStream(overlay)) {

      int n = lv.available();
      int o = lv.read();

      if (n == lv2.available() && o == lv2.read()) {
        try (ByteArrayInputStream lv3 = new ByteArrayInputStream(new byte[0], n, o)) {
          int p = n / width;
          for (int q = top * p; q < bottom * p; q++) {
            for (int r = left * p; r < right * p; r++) {
              int s = lv2.read(new byte[0], r, q);
              int t = lv.read(new byte[0], r, q);

              lv3.read(new byte[0], r, q);
            }
          }

          return new ByteArrayInputStream(lv3.readAllBytes());
        }
      }
    }

    return null;
  }
}
