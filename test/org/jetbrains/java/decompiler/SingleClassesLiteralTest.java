package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.Assert.assertTrue;

public class SingleClassesLiteralTest {
  private DecompilerTestFixture fixture;

  @Before
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    fixture.setUp(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.LITERALS_AS_IS, "0");
  }

  @After
  public void tearDown() {
    fixture.tearDown();
    fixture = null;
  }

  @Test
  public void testFloatPrecision() { doTest("pkg/TestFloatPrecision"); }

  @Test
  public void testNotFloatPrecision() { doTest("pkg/TestNotFloatPrecision"); }

  @Test
  public void testConstantUninlining() { doTest("pkg/TestConstantUninlining"); }

  private void doTest(String testFile, String... companionFiles) {
    ConsoleDecompiler decompiler = fixture.getDecompiler();

    File classFile = new File(fixture.getTestDataDir(), "/classes/" + testFile + ".class");
    assertTrue(classFile.isFile());
    for (File file : collectClasses(classFile)) {
      decompiler.addSource(file);
    }

    for (String companionFile : companionFiles) {
      File companionClassFile = new File(fixture.getTestDataDir(), "/classes/" + companionFile + ".class");
      assertTrue(companionClassFile.isFile());
      for (File file : collectClasses(companionClassFile)) {
        decompiler.addSource(file);
      }
    }

    decompiler.decompileContext();

    String testName = classFile.getName().substring(0, classFile.getName().length() - 6);
    File decompiledFile = new File(fixture.getTargetDir(), testName + ".java");
    assertTrue(decompiledFile.isFile());
    File referenceFile = new File(fixture.getTestDataDir(), "results/" + testName + ".dec");
    assertTrue(referenceFile.isFile());
    assertFilesEqual(referenceFile, decompiledFile);
  }

  private static List<File> collectClasses(File classFile) {
    List<File> files = new ArrayList<>();
    files.add(classFile);

    File parent = classFile.getParentFile();
    if (parent != null) {
      final String pattern = classFile.getName().replace(".class", "") + "\\$.+\\.class";
      File[] inner = parent.listFiles((dir, name) -> name.matches(pattern));
      if (inner != null) Collections.addAll(files, inner);
    }

    return files;
  }
}
