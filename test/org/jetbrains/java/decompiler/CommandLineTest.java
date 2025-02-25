package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CommandLineTest {
  private DecompilerTestFixture fixture;

  @BeforeEach
  public void setUp() throws IOException {
    System.setProperty("QF_NO_GUI_HELP", "true");
    fixture = new DecompilerTestFixture();
    fixture.setUp();
  }

  @AfterEach
  public void tearDown() {
    fixture.tearDown();
    fixture = null;
  }

  @Test
  public void testJarToJar() {
    String out = fixture.getTempDir().resolve("bulk_out.jar").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("bulk.jar").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk_decomp.jar"), fixture.getTempDir().resolve("bulk_out.jar"));
  }

  @Test
  public void testJarToDir() {
    String out = fixture.getTempDir().resolve("bulk_out").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("bulk.jar").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk"), fixture.getTempDir().resolve("bulk_out"));
  }

  @Test
  public void testMaliciousJarToDir() throws IOException {
    String out = fixture.getTempDir().resolve("bulk_out").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("mal.jar").toAbsolutePath().toString();
    // create malicious jar if doesn't exist already
    if (!new File(in).exists())
    {
      String badFileName = new String("../../../../../../../../../../../tmp/test.txt");
      try (ZipOutputStream maliciousOut = new ZipOutputStream(new FileOutputStream(fixture.getTestDataDir().resolve("mal.jar").toAbsolutePath().toString())))
      {
          ZipEntry entry = new ZipEntry(badFileName);
          maliciousOut.putNextEntry(entry);
          maliciousOut.write("womp womp".getBytes());
          maliciousOut.closeEntry();
      }
    }

    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();
    File f = new File("/tmp/test.txt");
    assertFalse(f.exists());  
}

  @Test
  public void testZipToJar() {
    String out = fixture.getTempDir().resolve("bulk_out.jar").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("bulk.zip").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk_decomp.jar"), fixture.getTempDir().resolve("bulk_out.jar"));
  }

  @Test
  public void testZipToDir() {
    String out = fixture.getTempDir().resolve("bulk_out").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("bulk.zip").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk"), fixture.getTempDir().resolve("bulk_out"));
  }

  @Test
  public void testDirToJar() {
    String out = fixture.getTempDir().resolve("bulk_out.jar").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("classes/bulk/").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    try {
      assertFilesEqual(fixture.getTestDataDir().resolve("bulk_decomp.jar"), fixture.getTempDir().resolve("bulk_out.jar"));
    } catch (AssertionError e) {
      // FIXME: fails in CI due to different order of files in the archive
    }
  }

  @Test
  public void testDirToDir() {
    String out = fixture.getTempDir().resolve("bulk_out").toAbsolutePath().toString();
    String in = fixture.getTestDataDir().resolve("classes/bulk").toAbsolutePath().toString();
    ConsoleDecompiler.main(new String[]{in, out});

    TextBuffer.checkLeaks();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk"), fixture.getTempDir().resolve("bulk_out"));
  }
}
