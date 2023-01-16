package org.jetbrains.java.decompiler.api.language;

import org.jetbrains.java.decompiler.api.GraphParser;
import org.jetbrains.java.decompiler.api.LanguageChooser;
import org.jetbrains.java.decompiler.api.StatementWriter;
import org.jetbrains.java.decompiler.api.passes.Pass;

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
