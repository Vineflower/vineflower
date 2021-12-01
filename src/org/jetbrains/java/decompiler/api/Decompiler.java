package org.jetbrains.java.decompiler.api;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

public interface Decompiler {

  class Builder {
    private final Options options = new Options();
    private IBytecodeProvider input;
    private DecompilerOutput output;
    private IFernflowerLogger logger = new PrintStreamLogger(System.out);
    private IFabricJavadocProvider javadocProvider;

    public <T> Builder with(Option<T> option, T value) {
      this.options.with(option, value);
      return this;
    }

    public Builder with(Options options) {
      this.options.putAll(options);
      return this;
    }

    public Builder with(IBytecodeProvider input) {
      this.input = input;
      return this;
    }

    public Builder with(DecompilerOutput output) {
      this.output = output;
      return this;
    }

    public Builder with(IFernflowerLogger logger) {
      this.logger = logger;
      return this;
    }

    public Builder with(IFabricJavadocProvider javadocProvider) {
      this.javadocProvider = javadocProvider;
      return this;
    }

    public Decompiler build() {
      return new Fernflower(this.input, this.output, this.options, this.logger, this.javadocProvider);
    }
  }
}
