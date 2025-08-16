package org.jetbrains.java.decompiler.ir;

import org.jetbrains.java.decompiler.DecompilerTestFixture;
import org.jetbrains.java.decompiler.MinimalQuiltflowerEnvironment;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.serializer.TapestryReader;
import org.jetbrains.java.decompiler.modules.serializer.TapestryWriter;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class IrTestBase {
  private final List<String> tests = new ArrayList<>();

  protected abstract void registerAll();

  protected abstract String transformName();

  protected abstract void runTransform(RootStatement root);

  protected void register(String name) {
    tests.add(name);
  }

  @TestFactory
  @DisplayName("Quiltflower IR Tests")
  public Stream<DynamicContainer> testRegistered() {
    tests.clear();
    registerAll();

    return Stream.of(
      DynamicContainer.dynamicContainer(transformName() + " Tests",
          makeTests()
        )
    );
  }

  protected Stream<DynamicNode> makeTests() {
    Path baseDir = new DecompilerTestFixture().getTestDataDir().resolve("transforms/" + transformName() + "/");
    Path outDir = new DecompilerTestFixture().getTestDataDir().resolve("results/transforms/" + transformName() + "/");

    return tests.stream().map(s -> {
      return DynamicTest.dynamicTest(s, outDir.resolve(s + ".res").toUri(), () -> {
        MinimalQuiltflowerEnvironment.setup();
        ImportCollector importCollector = new ImportCollector();
        DecompilerContext.startClass(importCollector);

        Path inPath = baseDir.resolve(s + ".qir");
        String input = Files.readString(inPath);
        RootStatement root = TapestryReader.readTapestry(input);
        int indexOf = input.indexOf(TapestryReader.BLOCK_SEP);
        if (indexOf > 0) {
          input = input.substring(0, indexOf).stripTrailing();
        }

        input = appendTransform(root, input);
        runTransform(root);

        String output = TapestryWriter.serialize(root);
        output = appendTransform(root, output);

        Path outPath = outDir.resolve(s + ".res");
        if (outPath.toFile().exists()) {
          assertEquals(Files.readString(outPath).replaceAll("\r\n", "\n"), output);

          String existing = Files.readString(inPath).replaceAll("\r\n", "\n");
          // Ensuring the input file has a rendered statement at the end
          assumeTrue(existing.equals(input), "input file must be\n\"\"\n" + input + "\n\"\"\n");
        } else {
          Files.writeString(outPath, output);
          assumeTrue(false, outPath + " was not present yet");
        }
      });
    });
  }

  private static String appendTransform(RootStatement root, String output) {
    return output + "\n" + TapestryReader.BLOCK_SEP + "\n" + root.toJava().convertToStringAndAllowDataDiscard().replaceAll("\r\n", "\n").stripTrailing();
  }
}
