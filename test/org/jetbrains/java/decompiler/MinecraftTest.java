package org.jetbrains.java.decompiler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MinecraftTest  extends SingleClassesTestBase {
  private static final String MC_PATH = "Z:\\Projects\\MCP\\BlueMining\\mcp_update\\new";
  private static final Map<String, String> RESULTS = new HashMap<>();
  private static final Gson GSON = new GsonBuilder().create();

  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.DECOMPILE_INNER,"1",
      IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES,"1",
      IFernflowerPreferences.ASCII_STRING_CHARACTERS,"1",
      IFernflowerPreferences.LOG_LEVEL, "TRACE",
      IFernflowerPreferences.REMOVE_SYNTHETIC, "1",
      IFernflowerPreferences.REMOVE_BRIDGE, "1",
      IFernflowerPreferences.USE_DEBUG_VAR_NAMES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "1"
    };
  }

  @Override
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture() {
      private ConsoleDecompiler decompiler;

      @Override
      public void setUp(String... optionPairs) throws IOException {
        assertThat(optionPairs.length % 2).isEqualTo(0);
        File tempDir = File.createTempFile("decompiler_test_", "_dir");
        assertThat(tempDir.delete()).isTrue();

        File targetDir = new File(tempDir, "decompiled");
        assertThat(targetDir.mkdirs()).isTrue();

        Map<String, Object> options = new HashMap<>();
        options.put(IFernflowerPreferences.LOG_LEVEL, "warn");
        options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
        options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
        options.put(IFernflowerPreferences.REMOVE_BRIDGE, "1");
        options.put(IFernflowerPreferences.LITERALS_AS_IS, "1");
        options.put(IFernflowerPreferences.UNIT_TEST_MODE, "1");
        for (int i = 0; i < optionPairs.length; i += 2) {
          options.put(optionPairs[i], optionPairs[i + 1]);
        }
        decompiler = new ConsoleDecompiler(targetDir, options, new PrintStreamLogger(System.out)) {
          @Override
          public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
            RESULTS.put(path, content);
          }

          @Override
          public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
            RESULTS.put(qualifiedName, content);
          }
        };
      }

      @Override
      public ConsoleDecompiler getDecompiler() {
        return decompiler;
      }
    };
    fixture.setUp(getDecompilerOptions());
    fixture.setCleanup(false);
  }

  @Override
  protected void doTest(String testFile, String... companionFiles) {
    File MC_JAR = new File(MC_PATH);
    if (!MC_JAR.exists()) {
      return;
    }

    RESULTS.clear();

    ConsoleDecompiler decompiler = fixture.getDecompiler();
    if (MC_JAR.isDirectory()) {
      //decompiler.addSpace(new File(MC_JAR, "jars\\libraries"), false);
      gatherLibraries(decompiler);
      decompiler.addSource(new File(MC_JAR, "temp\\minecraft_ff_in.jar"));
    }
    else {
      decompiler.addSource(MC_JAR);
    }
    decompiler.decompileContext();

    for (Entry<String, String> entry : RESULTS.entrySet()) {
      if (!entry.getKey().startsWith(testFile)) {
        continue;
      }
      System.out.println(entry.getKey());
      System.out.println(entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  private void gatherLibraries(ConsoleDecompiler decompiler) {
    try {
      String version = null;
      for (String line : Files.readAllLines(new File(MC_PATH, "conf/version.cfg").toPath())) {
        if (line.startsWith("ClientVersion")) {
          version = line.split(" ")[2];
        }
      }
      String data = new String(Files.readAllBytes(new File(MC_PATH, "jars/versions/" + version + "/" + version + ".json").toPath()));
      List<Map<String, Object>> libs = (List<Map<String, Object>>)GSON.fromJson(data, Map.class).get("libraries");

      for (Map<String, Object> e : libs) {
        String path = ((Map<String, Map<String, String>>)e.get("downloads")).get("artifact").get("path");
        File lib = new File(MC_PATH, "jars/libraries/" + path);
        decompiler.addLibrary(lib);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test public void testWoodlandMansionPieces() { doTest("net/minecraft/world/gen/structure/WoodlandMansionPieces"); }
  @Test public void testEntityPlayer() { doTest("net/minecraft/command/impl/SeedCommand"); }
}
