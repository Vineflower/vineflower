package org.vineflower.unpick.def;

import org.jetbrains.java.decompiler.util.Either;
import org.jetbrains.java.decompiler.util.Unit;

import java.util.List;

public class UnpickDefinition {
  private UnpickKind kind;
  private String target;
  private List<UnpickTarget> targets;
  // <Param, Return>
  private Either<Integer, Unit> location;
}
