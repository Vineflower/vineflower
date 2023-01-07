// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.vars;

import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;

public final class VarVersionEdge {

  public final VarVersionNode source;
  public final VarVersionNode dest;
  public final Type type2;

  private VarVersionEdge(VarVersionNode source, VarVersionNode dest, Type type) {
    ValidationHelper.notNull(source);
    ValidationHelper.notNull(dest);
    ValidationHelper.notNull(type);

    this.source = source;
    this.dest = dest;
    this.type2 = type;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof VarVersionEdge)) {
      return false;
    }

    VarVersionEdge edge = (VarVersionEdge)o;
    return this.type2 == edge.type2 && this.source == edge.source && this.dest == edge.dest;
  }

  @Override
  public int hashCode() {
    // TODO: why this weird hashcode?
    return this.source.hashCode() ^ this.dest.hashCode() + this.type2.ordinal();
  }

  @Override
  public String toString() {
    return this.type2 + ": " + this.source + " -> " + this.dest;
  }

  public static VarVersionEdge create(VarVersionNode source, VarVersionNode dest) {
    return create(Type.NORMAL, source, dest);
  }

  public static VarVersionEdge link(VarVersionNode source, VarVersionNode dest) {
    return create(Type.LINKED, source, dest);
  }

  public static VarVersionEdge create(Type type, VarVersionNode source, VarVersionNode dest) {
    VarVersionEdge edge = new VarVersionEdge(source, dest, type);
    source.addSuccessor(edge);
    dest.addPredecessor(edge);
    return edge;
  }

  public enum Type {
    NORMAL, LINKED
  }
}
