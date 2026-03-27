package org.vineflower.kotlin.expr;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinChooser;
import org.vineflower.kotlin.struct.KElement;
import org.vineflower.kotlin.struct.KFunction;
import org.vineflower.kotlin.struct.KProperty;
import org.vineflower.kotlin.util.KTypes;
import org.vineflower.kt.metadata.deserialization.Flags;

import java.util.Objects;

public class KInvocationExprent extends InvocationExprent implements KExprent {
  private final @Nullable KElement callingData;
  private final @Nullable KProperty.PropertyMethod propertyMethod;

  public KInvocationExprent(InvocationExprent expr) {
    super(expr);
    StructClass cl = DecompilerContext.getStructContext().getClass(expr.getClassname());
    if (cl == null) {
      callingData = null;
      propertyMethod = null;
      return;
    }

    StructMethod method = cl.getMethod(expr.getName(), expr.getStringDescriptor());
    if (method == null) {
      callingData = null;
      propertyMethod = null;
      return;
    }

    KotlinChooser.parseMetadataFor(cl);
    callingData = method.getAttribute(KElement.KEY);
    propertyMethod = method.getAttribute(KProperty.PropertyMethod.KEY);
  }

  @Override
  public int getPrecedence() {
    if (callingData instanceof KFunction function) {
      return Flags.IS_INFIX.get(function.flags()) ? 9 : super.getPrecedence();
    }
    return super.getPrecedence();
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (callingData instanceof KProperty property) {
      Objects.requireNonNull(propertyMethod);
      
      TextBuffer buf = new TextBuffer();
      buf.addBytecodeMapping(bytecode);
      if (getInstance() != null) {
        buf.append(getInstance().toJava(indent)).append('.');
      }
      buf.append(property.name());
      if (propertyMethod == KProperty.PropertyMethod.SETTER) {
        buf.append(" = ");
        Exprent newValue = getLstParameters().get(0);
        buf.append(newValue.toJava(indent));
      }
      return buf;
    }
    
    if (callingData instanceof KFunction function) {
      if (Flags.IS_INFIX.get(function.flags())) {
        TextBuffer buf = new TextBuffer();
        buf.addBytecodeMapping(bytecode);
        boolean isExtensionFunction = function.receiverType() != null;
        Exprent instance = isExtensionFunction ? getLstParameters().get(0) : getInstance();
        Exprent arg = isExtensionFunction ? getLstParameters().get(1) : getLstParameters().get(0);
        TextBuffer instanceBuf = instance.toJava(indent);
        if (getInstance().getPrecedence() > getPrecedence()) {
          instanceBuf.enclose("(", ")");
        }
        buf.append(instanceBuf)
          .append(" ")
          .append(function.name())
          .append(" ")
          .append(arg.toJava(indent));
        return buf;
      }
    }

    if (KTypes.isFunctionType(new VarType(getClassname(), true))) {
      TextBuffer buf = new TextBuffer();
      TextBuffer instanceBuf = getInstance().toJava(indent);
      if (getInstance().getPrecedence() > getPrecedence()) {
        instanceBuf.enclose("(", ")");
      }
      buf.append(instanceBuf);
      if (getLstParameters().isEmpty()) {
        buf.appendMethod("()", false, getClassname(), getName(), getDescriptor());
        buf.addBytecodeMapping(bytecode);
        return buf;
      }

      buf.appendMethod("(", false, getClassname(), getName(), getDescriptor());
      buf.append(appendParamList(indent));
      buf.appendMethod(")", false, getClassname(), getName(), getDescriptor());
      buf.addBytecodeMapping(bytecode);
      return buf;
    }

    return super.toJava(indent);
  }

  @Override
  public Exprent copy() {
    return new KInvocationExprent(this);
  }
}
