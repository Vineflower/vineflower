// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

import java.util.ArrayList;
import java.util.List;

// A connection between 2 statements.
// Describes edges in graphs where statements are the vertices.
public class StatEdge {
  // Represents direct control flow between 2 statements
  public static final int TYPE_REGULAR = 1;
  // Represents implicit control flow between any statement in a try block and the catch blocks
  // This is because any statement in the try block can throw and execution can flow to the catch blocks from there
  public static final int TYPE_EXCEPTION = 2;
  // Represents control flow out of a statement and to the next statement
  // Also represents returns
  public static final int TYPE_BREAK = 4;
  // Represents control flow back up to a previous statement, marking a loop
  public static final int TYPE_CONTINUE = 8;
  // Represents exits from finally blocks
  public static final int TYPE_FINALLYEXIT = 32;

  public static final int[] TYPES = new int[]{
    TYPE_REGULAR,
    TYPE_EXCEPTION,
    TYPE_BREAK,
    TYPE_CONTINUE,
    TYPE_FINALLYEXIT
  };

  private int type;

  private Statement source;

  private Statement destination;

  private List<String> exceptions;

  public Statement closure;

  // Whether this edge is labeled or not.
  public boolean labeled = true;

  // Whether this edge is explicitly defined or implicit.
  public boolean explicit = true;

  // Whether this edge can be inlined to simplify the decompile or not.
  public boolean canInline = true;

  public StatEdge(int type, Statement source, Statement destination, Statement closure) {
    this(type, source, destination);
    this.closure = closure;
  }

  public StatEdge(int type, Statement source, Statement destination) {
    this.type = type;
    this.source = source;
    this.destination = destination;
  }

  public StatEdge(Statement source, Statement destination, List<String> exceptions) {
    this(TYPE_EXCEPTION, source, destination);
    if (exceptions != null) {
      this.exceptions = new ArrayList<>(exceptions);
    }
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Statement getSource() {
    return source;
  }

  public void setSource(Statement source) {
    this.source = source;
  }

  public Statement getDestination() {
    return destination;
  }

  public void setDestination(Statement destination) {
    this.destination = destination;
  }

  public List<String> getExceptions() {
    return this.exceptions;
  }

  //	public void setException(String exception) {
  //		this.exception = exception;
  //	}

  @Override
  public String toString() {
    return this.type + ": " + this.source.toString() + " -> " + this.destination.toString() + ((this.closure == null) ? "" : " (" + this.closure + ")") + ((this.exceptions == null) ? "" : " Exceptions: " + this.exceptions);
  }
}
