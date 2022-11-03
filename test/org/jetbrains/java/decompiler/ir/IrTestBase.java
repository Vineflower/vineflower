package org.jetbrains.java.decompiler.ir;

import org.jetbrains.java.decompiler.DecompilerTestFixture;
import org.jetbrains.java.decompiler.MinimalQuiltflowerEnvironment;
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
      return DynamicTest.dynamicTest(s, () -> {
        MinimalQuiltflowerEnvironment.setup();
        String input = Files.readString(baseDir.resolve(s + ".qir"));
        RootStatement root = TapestryReader.readTapestry(input);
        runTransform(root);

        String output = TapestryWriter.serialize(root);

        Path outPath = outDir.resolve(s + ".qir");
        if (outPath.toFile().exists()) {
          assertEquals(Files.readString(outPath).replace("\r\n", "\n"), output);
        } else {
          assumeTrue(false, outPath + " was not present yet");
          Files.writeString(outPath, output);
        }
      });
    });
  }
}
