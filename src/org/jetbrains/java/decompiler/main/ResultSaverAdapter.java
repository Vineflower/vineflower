package org.jetbrains.java.decompiler.main;

import net.fabricmc.fernflower.api.IFabricResultSaver;
import org.jetbrains.java.decompiler.api.DecompilerOutput;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

class ResultSaverAdapter implements IFabricResultSaver {
  private final DecompilerOutput output;
  private final Map<String, DecompilerOutput> archives = new HashMap<>();

  public ResultSaverAdapter(DecompilerOutput output) {
    this.output = output;
  }

  private DecompilerOutput getArchive(String path, String archiveName) {
    return archives.computeIfAbsent(path + "/" + archiveName, name -> {
      try {
        return output.openArchive(name);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  @Override
  public void saveFolder(String path) {
    try {
      output.createDirectory(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
    try {
      output.writeFile(entryName, Files.readAllBytes(Paths.get(source)));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    try {
      output.writeClass(qualifiedName, content, mapping);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
    try {
      getArchive(path, archiveName).writeManifest(manifest);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
    try {
      getArchive(path, archiveName).createDirectory(entryName);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entry) {
    try {
      getArchive(path, archiveName).writeFile(entry, Files.readAllBytes(Paths.get(source)));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    saveClassEntry(path, archiveName, qualifiedName, entryName, content, null);
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping) {
    try {
      getArchive(path, archiveName).writeClass(qualifiedName, content, mapping);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void closeArchive(String path, String archiveName) {
    DecompilerOutput archive = archives.remove(path + "/" + archiveName);
    if (archive != null) {
      try {
        archive.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
