package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinChooser;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.struct.*;
import org.vineflower.kotlin.util.KTypes;

public class KFieldExprent extends FieldExprent implements KExprent {
  public KFieldExprent(FieldExprent field) {
    super(field.getName(), field.getClassname(), field.isStatic(), field.getInstance(), field.getDescriptor(), field.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();

    if (getName().equals("TYPE") && ExprUtil.PRIMITIVE_TYPES.containsKey(getClassname())) {
      buf.addBytecodeMapping(bytecode);
      VarType type = new VarType(getClassname(), true);
      buf.append(KTypes.getKotlinType(type));
      buf.append("::class.javaPrimitiveType");
      return buf;
    }
    if (getName().equals("INSTANCE")) {
      StructClass cl = DecompilerContext.getStructContext().getClass(getClassname());
      if (cl == null) {
        return super.toJava(indent);
      }

      KotlinChooser.parseMetadataFor(cl);

      KElement ktData = cl.getAttribute(KElement.KEY);
      if (ktData == null) {
        return super.toJava(indent);
      }

      if (ktData instanceof KClass cls) {
        if (cls.proto().hasCompanionObjectName()) {
          String name = cls.resolver() == null ? cl.qualifiedName : cls.resolver().resolve(cls.proto().getCompanionObjectName());
          buf.appendClass(DecompilerContext.getImportCollector().getShortName(cl.qualifiedName), false, name);
          buf.addBytecodeMapping(bytecode);
          return buf;
        }
      } else if (ktData instanceof KFunction function) {
        ClassWrapper wrapper = DecompilerContext.getClassProcessor().getMapRootClasses().get(getClassname()).getWrapper();
        MethodWrapper method = function.methodSupplier().apply(wrapper);

        buf.append("{");

        int index = 0;
        for (KParameter param : function.parameters()) {
          if (index > 0) {
            buf.append(", ");
          } else {
            buf.append(" ");
          }
          buf.appendVariable(param.name(), true, true, function.classStruct().qualifiedName, function.methodStruct().getName(), function.methodStruct().getDescriptor(), index, param.name());
          index += param.type().stackSize;
        }

        if (function.parameters().length > 0) {
          buf.append(" ->");
        }

        buf.appendLineSeparator();

        MethodWrapper outerMethod = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
        try {
          if (method.decompileError == null) {
            RootStatement root = method.root;
            if (root != null) {
              DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, method);
              TextBuffer body = root.toJava(indent + 1);
              body.addBytecodeMapping(root.getDummyExit().bytecode);
              buf.append(body, cl.qualifiedName, InterpreterUtil.makeUniqueKey(method.methodStruct.getName(), method.methodStruct.getDescriptor()));
            }
          }
        } finally {
          DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, outerMethod);
        }

        if (method.decompileError != null) {
          KotlinWriter.dumpError(buf, method, indent + 1);
        }

        buf.appendIndent(indent).append("}");
        buf.addBytecodeMapping(bytecode);
        return buf;
      } else if (ktData instanceof KFunctionReference) {
        TextBuffer buffer = KotlinWriter.stringifyReference(indent, DecompilerContext.getClassProcessor().getMapRootClasses().get(getClassname()), bytecode, null);
        if (buffer != null) {
          return buffer;
        }
      }
    }
    return super.toJava(indent);
  }
}
