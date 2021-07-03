/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/*
 * Set individual test duration time limit to 60 seconds.
 * This will help us to test bugs hanging decompiler.
 */
@Timeout(60)
public abstract class SingleClassesTestBase {
  protected final List<TestDefinition> testDefinitions = new ArrayList<>();
  protected DecompilerTestFixture fixture;
  
  protected String[] getDecompilerOptions() {
    return new String[] {};
  }

  protected abstract void registerAll();

  protected void register(TestDefinition.Version version, String testClass, String... others) {
    List<String> othersList = new ArrayList<>(others.length);
    for (String other : others) othersList.add(getFullClassName(other));
    testDefinitions.add(new TestDefinition(version, getFullClassName(testClass), othersList));
  }

  protected void registerRaw(TestDefinition.Version version, String testClass, String ...others) {
    testDefinitions.add(new TestDefinition(version, testClass, Arrays.asList(others)));
  }

  private static String getFullClassName(String className) {
    if (!className.contains("/")) return "pkg/" + className;
    return className;
  }

  @BeforeEach
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    fixture.setUp(getDecompilerOptions());
  }

  @AfterEach
  public void tearDown() {
    if (fixture == null) return;
    fixture.tearDown();
    fixture = null;
  }

  @TestFactory
  @DisplayName("Test single classes")
  public List<DynamicTest> testRegistered() {
    testDefinitions.clear();
    registerAll();
    List<DynamicTest> tests = new ArrayList<>();
    for (TestDefinition def : testDefinitions) {
      String name = def.testClass;
      int slash = name.lastIndexOf('/');
      if (slash >= 0) name = name.substring(slash + 1);
      name = def.version + ": " + name;
      tests.add(DynamicTest.dynamicTest(name, () -> {
        setUp();
        doTest(def.version, def.testClass, def.others.toArray(new String[0]));
        tearDown();
      }));
    }
    return tests;
  }

  protected Path getClassFile(TestDefinition.Version version, String name) {
    return fixture.getTestDataDir().resolve("classes/" + version.directory + "/" + name + ".class");
  }

  protected Path getReferenceFile(String testClass) {
    return fixture.getTestDataDir().resolve("results/" + testClass + ".dec");
  }

  protected void doTest(TestDefinition.Version version, String testFile, String... companionFiles) {
    ConsoleDecompiler decompiler = fixture.getDecompiler();

    Path classFile = getClassFile(version, testFile);
    assertTrue(Files.isRegularFile(classFile), classFile + " should exist");
    for (Path file : collectClasses(classFile)) {
      decompiler.addSource(file.toFile());
    }

    for (String companionFile : companionFiles) {
      Path companionClassFile = getClassFile(version, companionFile);
      assertTrue(Files.isRegularFile(companionClassFile), companionFile + " should exist");
      for (Path file : collectClasses(companionClassFile)) {
        decompiler.addSource(file.toFile());
      }
    }

    decompiler.decompileContext();

    String testFileName = classFile.getFileName().toString();
    String testName = testFileName.substring(0, testFileName.length() - 6);
    Path decompiledFile = fixture.getTargetDir().resolve(testName + ".java");
    assertTrue(Files.isRegularFile(decompiledFile));
    Path referenceFile = getReferenceFile(testFile);
    if (!Files.exists(referenceFile)) {
      try {
        Files.createDirectories(referenceFile.getParent());
        Files.copy(decompiledFile, referenceFile);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      //noinspection ConstantConditions
      assumeTrue(false, referenceFile.getFileName() + " was not present yet");
    } else {
      assertTrue(Files.isRegularFile(referenceFile));
      assertFilesEqual(referenceFile, decompiledFile);
    }
  }

  private static List<Path> collectClasses(Path classFile) {
    List<Path> files = new ArrayList<>();
    files.add(classFile);

    Path parent = classFile.getParent();
    if (parent != null) {
      final String pattern = classFile.getFileName().toString().replace(".class", "") + "\\$.+\\.class";
      try {
        Files.list(parent).filter(p -> p.getFileName().toString().matches(pattern)).forEach(files::add);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return files;
  }

  static class TestDefinition {
    public final Version version;
    public final String testClass;
    public final List<String> others;

    public TestDefinition(Version version, String testClass, List<String> others) {
      this.version = version;
      this.testClass = testClass;
      this.others = others;
    }

    enum Version {
      CUSTOM("custom", "Custom"),
      JAVA_8(8),
      JAVA_9(9),
      JAVA_11(11),
      JAVA_16(16),
      GROOVY("groovy", "Groovy"),
      KOTLIN("kt", "Kotlin"),
      JASM("jasm", "Custom (jasm)"),
      ;

      public final String directory;
      public final String display;

      Version(String directory, String display) {
        this.directory = directory;
        this.display = display;
      }

      Version(int javaVersion) {
        this("java" + javaVersion, "Java " + javaVersion);
      }

      @Override
      public String toString() {
        return display;
      }
    }
  }
}