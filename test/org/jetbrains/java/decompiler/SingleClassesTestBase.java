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

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Set individual test duration time limit to 60 seconds.
 * This will help us to test bugs hanging decompiler.
 */
@Timeout(60)
public abstract class SingleClassesTestBase {
  protected final Map<String, List<String>> testDefinitions = new LinkedHashMap<>();
  protected DecompilerTestFixture fixture;
  
  protected String[] getDecompilerOptions() {
    return new String[] {};
  }

  protected abstract void registerAll();

  protected void register(String testClass, String ...others) {
    List<String> othersList = new ArrayList<>(others.length);
    for (String other : others) othersList.add(getFullClassName(other));
    testDefinitions.put(getFullClassName(testClass), othersList);
  }

  protected void registerRaw(String testClass, String ...others) {
    testDefinitions.put(testClass, Arrays.asList(others));
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
    for (Map.Entry<String, List<String>> def : testDefinitions.entrySet()) {
      String testFile = def.getKey();
      String name = testFile;
      int slash = name.lastIndexOf('/');
      if (slash >= 0) name = name.substring(slash + 1);
      String[] others = def.getValue().toArray(new String[0]);
      tests.add(DynamicTest.dynamicTest(name, () -> {
        setUp();
        doTest(testFile, others);
        tearDown();
      }));
    }
    return tests;
  }

  protected void doTest(String testFile, String... companionFiles) {
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