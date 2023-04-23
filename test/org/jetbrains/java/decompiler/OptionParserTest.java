package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.OptionParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class OptionParserTest {
  Map<String, Object> expected = Map.of(
    "decompile-generics", "1",
    "decompile-java4", "1",
    "remove-synthetic", "0",
    "remove-bridge", "0"
  );

  // Requires that short options are parsed into their long counterparts
  @Test
  public void testShortOptions() {
    Map<String, Object> result = new HashMap<>();

    String[] tests = {
      "-dgs=1",
      "-dc4=true",
      "-rsy=0",
      "-rbr=false",
    };

    for (String test : tests) {
      OptionParser.parse(test, result);
    }

    Assertions.assertEquals(expected, result);
  }

  // Requires that a mix of short and long options are parsed correctly
  @Test
  public void testMixedOptions() {
    Map<String, Object> result = new HashMap<>();

    String[] tests = {
      "-dgs=1",
      "--decompile-java4=true",
      "-rsy=0",
      "--remove-bridge=false",
    };

    for (String test : tests) {
      OptionParser.parse(test, result);
    }

    Assertions.assertEquals(expected, result);
  }

  // Requires that shorthand boolean options are parsed correctly
  @Test
  public void testBooleanOptions() {
    Map<String, Object> result = new HashMap<>();

    String[] tests = {
      "--decompile-generics",
      "--decompile-java4",
      "--no-remove-synthetic",
      "--no-remove-bridge",
    };

    for (String test : tests) {
      OptionParser.parse(test, result);
    }

    Assertions.assertEquals(expected, result);
  }
}
