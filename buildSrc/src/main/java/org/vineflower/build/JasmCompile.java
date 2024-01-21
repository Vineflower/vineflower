package org.vineflower.build;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ReplacedBy;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.openjdk.asmtools.jasm.Main;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JasmCompile extends SourceTask {
  private final DirectoryProperty destinationDirectory = this.getProject().getObjects().directoryProperty();
  private final JasmCompileOptions compileOptions = this.getProject().getObjects().newInstance(JasmCompileOptions.class);

  @OutputDirectory
  public DirectoryProperty getDestinationDirectory() {
    return this.destinationDirectory;
  }

  @ReplacedBy("destinationDirectory")
  public File getDestinationDir() {
    return this.destinationDirectory.getAsFile().getOrNull();
  }

  public void setDestinationDir(File destinationDir) {
    this.destinationDirectory.set(destinationDir);
  }

  public void setDestinationDir(Provider<File> destinationDir) {
    this.destinationDirectory.set(this.getProject().getLayout().dir(destinationDir));
  }

  @TaskAction
  protected void compile() {
    List<String> args = new ArrayList<>(compileOptions.getCompilerArgs());
    args.add("-d");
    File destinationDir = getDestinationDir();
    //noinspection ResultOfMethodCallIgnored
    destinationDir.mkdirs();
    args.add(destinationDir.toString());
    for (File sourceFile : getSource().getFiles()) {
      if (!sourceFile.getName().endsWith(".jasm")) continue;
      args.add(sourceFile.toString());
    }
    Main jasm = new Main(new PrintWriter(System.out), "jasm");
    if (!jasm.compile(args.toArray(new String[0]))) {
      throw new RuntimeException("jasm compile failed");
    }
  }

  @Nested
  public JasmCompileOptions getOptions() {
    return this.compileOptions;
  }
}
