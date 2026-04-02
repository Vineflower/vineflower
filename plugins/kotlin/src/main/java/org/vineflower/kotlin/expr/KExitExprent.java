package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.vineflower.kotlin.struct.KType;

public class KExitExprent extends ExitExprent implements KExprent {
  private String lambdaName;
  private boolean isLambda = false;

  public KExitExprent(ExitExprent expr) {
    super(expr.getExitType(), expr.getValue(), expr.getRetType(), expr.bytecode, expr.getMethodDescriptor());
  }
  
  public void setLambdaName(String name) {
    lambdaName = name;
    isLambda = true;
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (getExitType().equals(Type.THROW)) {
      return super.toJava(indent);
    }

    MethodWrapper outerMethod = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
    if (outerMethod != null) {
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);

      if (!isLambda || lambdaName != null) {
        buf.appendKeyword("return");
        if (lambdaName != null) {
          buf.appendPunctuation("@").append(lambdaName, TokenType.LABEL);
        }

        if (VarType.VARTYPE_VOID.equals(getRetType()) || KType.UNIT.equals(getRetType())) {
          return buf;
        }

        buf.appendWhitespace(" ");
      }

      //TODO check if not casting ever breaks something
      return buf.append(getValue().toJava(indent));
    }

    return super.toJava(indent);
  }

  @Override
  public Exprent copy() {
    return new KExitExprent((ExitExprent) super.copy());
  }
}
