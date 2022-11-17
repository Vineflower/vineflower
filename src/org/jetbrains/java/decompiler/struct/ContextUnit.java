// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContextUnit {
  private final IContextSource source;
  private final boolean own;
  private final boolean root;

  private final IResultSaver resultSaver;
  private final IDecompiledData decompiledData;

  private volatile boolean entriesInitialized;
  private List<String> classEntries = List.of(); // class file or jar/zip entry
  private List<String> dirEntries = List.of();
  private List<IContextSource.Entry> otherEntries = List.of();
  private List<IContextSource> childContexts = List.of();

  public ContextUnit(IContextSource source, boolean own, boolean root, IResultSaver resultSaver, IDecompiledData decompiledData) {
    this.source = source;
    this.own = own;
    this.root = root;
    this.resultSaver = resultSaver;
    this.decompiledData = decompiledData;
  }

  private void initEntries() {
    if (!this.entriesInitialized) {
      synchronized (this) {
        if (!this.entriesInitialized) {
          final IContextSource.Entries entries = this.source.getEntries();
          // TODO: more proper handling of multirelease jars, rather than just stripping them
          this.classEntries = entries.classes().stream()
            .filter(ent -> ent.multirelease() == IContextSource.Entry.BASE_VERSION)
            .map(entry -> entry.basePath())
            .collect(Collectors.toUnmodifiableList());
          this.dirEntries = entries.directories();
          boolean includeExtras = !DecompilerContext.getOption(IFernflowerPreferences.SKIP_EXTRA_FILES);
          this.otherEntries = new ArrayList<>();
          for (final IContextSource.Entry entry : entries.others()) {
            if ("fernflower_abstract_parameter_names.txt".equals(entry.basePath())) {
              try (final InputStream is = this.source.getInputStream(entry)) {
                final byte[] data = is.readAllBytes();
                DecompilerContext.getStructContext().loadAbstractMetadata(new String(data, StandardCharsets.UTF_8));
              } catch (final IOException ex) {
                DecompilerContext.getLogger().writeMessage("Failed to load abstract parameter names file", IFernflowerLogger.Severity.ERROR, ex);
              }
            } else if (includeExtras) {
              this.otherEntries.add(entry);
            }
          }
          this.childContexts = entries.childContexts();
          this.entriesInitialized = true;
        }
      }
    }
  }

  public List<String> getClassNames() {
    this.initEntries();
    return this.classEntries;
  }

  public byte/* @Nullable */[] getClassBytes(final String className) throws IOException {
    return this.source.getClassBytes(className);
  }

  public List<String> getDirectoryNames() {
    this.initEntries();
    return this.dirEntries;
  }

  public List<IContextSource.Entry> getOtherEntries() {
    this.initEntries();
    return this.otherEntries;
  }

  public List<IContextSource> getChildContexts() {
    this.initEntries();
    return this.childContexts;
  }

  public String getName() {
    return this.source.getName();
  }

  public void clear() throws IOException {
    synchronized (this) {
      this.entriesInitialized = false;
      this.classEntries = List.of();
      this.dirEntries = List.of();
      this.otherEntries = List.of();
    }
  }

  public void save(final Function<String, StructClass> loader) throws IOException {
    this.initEntries();
    final IContextSource.IOutputSink sink = this.source.createOutputSink(this.resultSaver);
    if (sink == null) {
      throw new IllegalStateException("Context source " + this.source + " cannot be saved, but had a save requested.");
    }

    sink.begin();

    // directory entries
    for (String dirEntry : dirEntries) {
      sink.acceptDirectory(dirEntry);
    }

    // non-class entries
    for (IContextSource.Entry otherEntry : otherEntries) {
      sink.acceptOther(otherEntry.path());
    }

    //Whooo threads!
    final List<Future<?>> futures = new LinkedList<>();
    final ExecutorService decompileExecutor = Executors.newFixedThreadPool(Integer.parseInt((String) DecompilerContext.getProperty(IFernflowerPreferences.THREADS)));
    final DecompilerContext rootContext = DecompilerContext.getCurrentContext();
    final ClassContext[] toDump = new ClassContext[classEntries.size()];

    // classes
    for (int i = 0; i < classEntries.size(); i++) {
      StructClass cl = loader.apply(classEntries.get(i));
      String entryName = decompiledData.getClassEntryName(cl, classEntries.get(i));
      if (entryName != null) {
        final int finalI = i;
        futures.add(decompileExecutor.submit(() -> {
          setContext(rootContext);
          String content = decompiledData.getClassContent(cl);
          int[] mapping = null;
          if (DecompilerContext.getOption(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING)) {
            mapping = DecompilerContext.getBytecodeSourceMapper().getOriginalLinesMapping();
          }
          toDump[finalI] = new ClassContext(cl.qualifiedName, entryName, content, mapping);
        }));
      }
    }

    decompileExecutor.shutdown();

    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }

    for (final ClassContext cls : toDump) {
      if (cls != null) {
        sink.acceptClass(cls.qualifiedName, cls.entryName, cls.classContent, cls.mapping);
      }
    }

    sink.close();
  }

  public void setContext(DecompilerContext rootContext) {
    DecompilerContext current = DecompilerContext.getCurrentContext();
    if (current == null) {
      current = new DecompilerContext(
        new HashMap<>(rootContext.properties),
        rootContext.logger,
        rootContext.structContext,
        rootContext.classProcessor,
        rootContext.poolInterceptor,
        rootContext.renamerFactory
      );
      DecompilerContext.setCurrentContext(current);
    }
  }

  public boolean isOwn() {
    return own;
  }

  public boolean isRoot() {
    return this.root;
  }

  void close() throws Exception {
    if (this.source instanceof AutoCloseable) {
      ((AutoCloseable) this.source).close();
    }
    this.clear();
  }

  static final class ClassContext {
    private final String qualifiedName;
    private final String entryName;
    private final String classContent;
    private final int /* @Nullable */[] mapping;

    ClassContext(final String qualifiedName, final String entryName, final String classContent, final int[] mapping) {
      this.qualifiedName = qualifiedName;
      this.entryName = entryName;
      this.classContent = classContent;
      this.mapping = mapping;
    }
  }
}
