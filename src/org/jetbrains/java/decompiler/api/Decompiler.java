package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.Either;

import java.io.File;
import java.util.*;

public final class Decompiler {
  private final Fernflower engine;

  private Decompiler(Fernflower engine) {
    this.engine = engine;
  }

  public void decompile() {
    try {
      this.engine.decompileContext();
    } finally {
      this.engine.clearContext();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final List<Either<IContextSource, File>> sources = new ArrayList<>();
    private final List<Either<IContextSource, File>> libraries = new ArrayList<>();
    private final List<String> allowedPrefixes = new ArrayList<>();
    private IResultSaver saver = null;
    private IFernflowerLogger logger = IFernflowerLogger.NO_OP;
    private final Map<String, Object> options = new HashMap<>();

    public Builder inputs(IContextSource... sources) {
      for (IContextSource source : sources) {
        this.sources.add(Either.left(source));
      }

      return this;
    }

    public Builder inputs(File... files) {
      for (File file : files) {
        this.sources.add(Either.right(file));
      }

      return this;
    }

    public Builder output(IResultSaver saver) {
      this.saver = saver;

      return this;
    }

    public Builder logger(IFernflowerLogger logger) {
      this.logger = logger;

      return this;
    }

    public Builder option(String key, Object value) {
      if (value instanceof Boolean) {
        boolean bl = (Boolean)value;
        value = bl ? "1" : "0";
      }

      if ("true".equals(value)) {
        value = "1";
      }
      if ("false".equals(value)) {
        value = "0";
      }

      this.options.put(key, value);

      return this;
    }

    public Builder options(Object... values) {
      if (values.length % 2 != 0) {
        throw new IllegalArgumentException("provided values must be in the format 'key, pair'");
      }
      for (int i = 0; i < values.length; i += 2) {
        Object key = values[i];
        Object value = values[i + 1];

        if (!(key instanceof String)) {
          throw new IllegalArgumentException("key must be a string!");
        }

        option((String) key, value);
      }

      return this;
    }

    public Builder libraries(IContextSource... sources) {
      for (IContextSource source : sources) {
        this.libraries.add(Either.left(source));
      }

      return this;
    }

    public Builder libraries(File... files) {
      for (File file : files) {
        this.libraries.add(Either.right(file));
      }

      return this;
    }

    public Builder allowedPrefixes(String... strings) {
      this.allowedPrefixes.addAll(Arrays.asList(strings));

      return this;
    }

    public Decompiler build() {
      if (saver == null) {
        throw new IllegalArgumentException("Decompiler needs an output to write to!");
      }

      if (sources.isEmpty()) {
        throw new IllegalArgumentException("Decompiler needs at least one input!");
      }

      Fernflower engine = new Fernflower(this.saver, this.options, this.logger);

      for (Either<IContextSource, File> source : this.sources) {
        source.map(engine::addSource, engine::addSource);
      }

      for (Either<IContextSource, File> source : this.libraries) {
        source.map(engine::addLibrary, engine::addLibrary);
      }

      for (String prefix : this.allowedPrefixes) {
        engine.addWhitelist(prefix);
      }

      return new Decompiler(engine);
    }
  }
}
