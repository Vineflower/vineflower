// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BulkDecompilationTest {
  private DecompilerTestFixture fixture;

  @BeforeEach
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    fixture.setUp();
  }

  @AfterEach
  public void tearDown() {
    fixture.tearDown();
    fixture = null;
  }

  @Test
  public void testDirectory() {
    Path classes = fixture.getTempDir().resolve("classes");
    unpack(fixture.getTestDataDir().resolve("bulk.jar"), classes);

    ConsoleDecompiler decompiler = fixture.getDecompiler();
    decompiler.addSource(classes.toFile());
    decompiler.decompileContext();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk"), fixture.getTargetDir());

    TextBuffer.checkLeaks();
  }

  @Test
  public void testJar() {
    doTestJar("bulk");
  }

  @Test
  public void testKtJar() {
    doTestJar("kt25937");
  }

  // TODO: This test crashses, deadlocks, and throws OutOfMemoryErrors.
//  @Test
//  public void testObfuscated() {
//    doTestJar("obfuscated");
//  }

  private void doTestJar(String name) {
    ConsoleDecompiler decompiler = fixture.getDecompiler();
    String jarName = name + ".jar";
    decompiler.addSource(fixture.getTestDataDir().resolve(jarName).toFile());
    decompiler.decompileContext();

    Path unpacked = fixture.getTempDir().resolve("unpacked");
    unpack(fixture.getTargetDir().resolve(jarName), unpacked);

    assertFilesEqual(fixture.getTestDataDir().resolve(name), unpacked);

    TextBuffer.checkLeaks();
  }

  private static void unpack(Path archive, Path targetDir) {
    try (ZipFile zip = new ZipFile(archive.toFile())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          File file = new File(targetDir.toFile(), entry.getName());
          assertTrue(file.getParentFile().mkdirs() || file.getParentFile().isDirectory());
          try (InputStream in = zip.getInputStream(entry); OutputStream out = new FileOutputStream(file)) {
            InterpreterUtil.copyStream(in, out);
          }
        }
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}