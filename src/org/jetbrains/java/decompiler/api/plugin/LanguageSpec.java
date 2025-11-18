package org.jetbrains.java.decompiler.api.plugin;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;

public final class LanguageSpec {
  public final String name;
  public final LanguageChooser chooser;
  public final GraphParser graphParser;
  public final StatementWriter writer;
  public final Pass pass;
  public final Pass cfgPass;
  public final String extension;

  public LanguageSpec(String name, LanguageChooser chooser, GraphParser graphParser, StatementWriter writer, Pass pass, Pass cfgPass, String extension) {
    this.name = name;
    this.chooser = chooser;
    this.graphParser = graphParser;
    this.writer = writer;
    this.pass = pass;
    this.cfgPass = cfgPass;
    this.extension = extension;
  }
}
