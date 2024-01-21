package org.jetbrains.java.decompiler.api.plugin;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;

public final class LanguageSpec {
  public final String name;
  public final LanguageChooser chooser;
  public final GraphParser graphParser;
  public final StatementWriter writer;
  public final Pass pass;

  public LanguageSpec(String name, LanguageChooser chooser, GraphParser graphParser, StatementWriter writer, Pass pass) {
    this.name = name;
    this.chooser = chooser;
    this.graphParser = graphParser;
    this.writer = writer;
    this.pass = pass;
  }
}
