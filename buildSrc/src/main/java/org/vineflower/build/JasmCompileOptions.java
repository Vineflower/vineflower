package org.vineflower.build;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.compile.AbstractOptions;

import java.util.ArrayList;
import java.util.List;

public class JasmCompileOptions extends AbstractOptions {
  private List<String> compilerArgs = new ArrayList<>();

  @Input
  public List<String> getCompilerArgs() {
    return this.compilerArgs;
  }

  public void setCompilerArgs(List<String> compilerArgs) {
    this.compilerArgs = compilerArgs;
  }
}
