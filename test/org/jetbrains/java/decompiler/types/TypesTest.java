package org.jetbrains.java.decompiler.types;

import org.jetbrains.java.decompiler.MinimalFernflowerEnvironment;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypesTest {
  @Test
  public void intJoins() {
    MinimalFernflowerEnvironment.setup();

    // lower type join with int => int
    Assertions.assertEquals(VarType.VARTYPE_INT, VarType.join(VarType.VARTYPE_INT, VarType.VARTYPE_BYTECHAR));
    Assertions.assertEquals(VarType.VARTYPE_INT, VarType.join(VarType.VARTYPE_INT, VarType.VARTYPE_SHORTCHAR));
    Assertions.assertEquals(VarType.VARTYPE_INT, VarType.join(VarType.VARTYPE_INT, VarType.VARTYPE_CHAR));
    Assertions.assertEquals(VarType.VARTYPE_INT, VarType.join(VarType.VARTYPE_INT, VarType.VARTYPE_SHORT));
    Assertions.assertEquals(VarType.VARTYPE_INT, VarType.join(VarType.VARTYPE_INT, VarType.VARTYPE_BYTE));
  }

  // TODO: add the rest
}
