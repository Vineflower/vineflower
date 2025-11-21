package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.metadata.KotlinMetadata;
import org.vineflower.kotlin.util.KTypes;

public class KFieldExprent extends FieldExprent implements KExprent {
  public KFieldExprent(FieldExprent field) {
    super(field.getName(), field.getClassname(), field.isStatic(), field.getInstance(), field.getDescriptor(), field.bytecode);
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();

    if (getName().equals("TYPE") && ExprUtil.PRIMITIVE_TYPES.containsKey(getClassname())) {
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

      KotlinMetadata ktData = cl.getAttribute(KotlinMetadata.KEY);
      if (ktData == null) {
        return super.toJava(indent);
      }

      if (ktData.metadata instanceof KotlinMetadata.Class cls) {
        if (cls.proto().hasCompanionObjectName()) {
          String name = ktData.nameResolver == null ? cl.qualifiedName : ktData.nameResolver.resolve(cls.proto().getCompanionObjectName());
          buf.appendClass(DecompilerContext.getImportCollector().getShortName(cl.qualifiedName), false, name);
          return buf;
        }
      }
    }
    return super.toJava(indent);
  }
}
