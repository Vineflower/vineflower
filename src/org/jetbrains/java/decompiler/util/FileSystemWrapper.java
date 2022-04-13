package org.jetbrains.java.decompiler.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class FileSystemWrapper extends FileSystem {
  private static final Map<Path, FileSystemWrapper> ZIP_FILES = new HashMap<>();
  private final FileSystem wrapped;
  private final Predicate<FileSystem> onClose;
  private int refCount;

  public FileSystemWrapper(FileSystem fs, Predicate<FileSystem> onClose) {
    this.wrapped = fs;
    this.onClose = onClose;
  }

  @Override
  public FileSystemProvider provider() {
    return wrapped.provider();
  }

  public FileSystemWrapper addRef() {
    refCount++;
    return this;
  }

  @Override
  public void close() throws IOException {
    if (--refCount <= 0 && onClose.test(wrapped)) {
      wrapped.close();
    }
  }

  @Override
  public boolean isOpen() {
    return wrapped.isOpen();
  }

  @Override
  public boolean isReadOnly() {
    return wrapped.isReadOnly();
  }

  @Override
  public String getSeparator() {
    return wrapped.getSeparator();
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return wrapped.getRootDirectories();
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return wrapped.getFileStores();
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return wrapped.supportedFileAttributeViews();
  }

  @Override
  public Path getPath(String first, String... more) {
    return wrapped.getPath(first, more);
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    return wrapped.getPathMatcher(syntaxAndPattern);
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return wrapped.getUserPrincipalLookupService();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return wrapped.newWatchService();
  }

  public static FileSystem getZipFileSystem(Path file) throws IOException {
    try {
      return ZIP_FILES.computeIfAbsent(file, f -> {
        URI uri = null;
        try {
          uri = new URI("jar:file", null, f.toUri().getPath(), null);
          return new FileSystemWrapper(FileSystems.newFileSystem(uri, Collections.emptyMap()), fs -> {
            ZIP_FILES.remove(f);
            return true;
          });
        } catch (FileSystemAlreadyExistsException e) {
          return new FileSystemWrapper(FileSystems.getFileSystem(uri), fs -> {
            ZIP_FILES.remove(f);
            return false;
          });
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }).addRef();
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }
}
