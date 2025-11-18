package org.vineflower.build;

import java.io.Serializable;
import org.gradle.api.tasks.Input;

import java.util.ArrayList;
import java.util.List;

public class JasmCompileOptions implements Serializable {
  private List<String> compilerArgs = new ArrayList<>();

  @Input
  public List<String> getCompilerArgs() {
    return this.compilerArgs;
  }

  public void setCompilerArgs(List<String> compilerArgs) {
    this.compilerArgs = compilerArgs;
  }
}
