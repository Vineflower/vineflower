package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.decompiler.CancelationManager;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.util.KTypes;

import java.util.ArrayList;
import java.util.List;

public class KNewExprent extends NewExprent implements KExprent {
  public KNewExprent(NewExprent expr) {
    super(expr.getNewType(), expr.getLstDims(), expr.bytecode);
    setConstructor(expr.getConstructor());
    setLstArrayElements(expr.getLstArrayElements());
    setDirectArrayInit(expr.isDirectArrayInit());
    setVarArgParam(expr.isVarArgParam());
    setAnonymous(expr.isAnonymous());

    assert expr.isLambda() == isLambda();
  }

  @Override
  public int getPrecedence() {
    // Kotlin treats constructors as not much more than slightly fancy functions
    return 0;
  }

  @Override
  public TextBuffer toJava(int indent) {
    if (isLambda()) {
      MethodWrapper outerWrapper = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
      try {
        ClassesProcessor.ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(getNewType().value);
        ClassesProcessor.ClassNode.LambdaInformation lambdaInfo = node.lambdaInformation;
        String name = lambdaInfo.content_method_name;
        if (name.contains("$lambda$")) {
          //TODO perform more exhaustive checks
          MethodDescriptor referencedDesc = MethodDescriptor.parseDescriptor(lambdaInfo.method_descriptor);
          MethodDescriptor realDesc = MethodDescriptor.parseDescriptor(lambdaInfo.content_method_descriptor);

          MethodWrapper wrapper = node.getWrapper().getMethodWrapper(name, lambdaInfo.content_method_descriptor);
          TextBuffer buf = new TextBuffer();
          buf.append("{ ");

          if (referencedDesc.params.length > 0) {
            int index = lambdaInfo.is_content_method_static ? 0 : 1;
            int startIndex = realDesc.params.length - referencedDesc.params.length;
            for (int i = 0; i < realDesc.params.length; i++) {
              if (i < startIndex) {
                index += realDesc.params[i].stackSize;
                continue;
              } else if (i > startIndex) {
                buf.append(", ");
              }

              VarType type = realDesc.params[i];
              String clashingName = wrapper.varproc.getClashingName(new VarVersionPair(index, 0));
              String parameterName = wrapper.varproc.getVarName(new VarVersionPair(index, 0));
              if (clashingName != null) {
                parameterName = clashingName;
              } else if (parameterName == null) {
                parameterName = "param" + index;
              }

              parameterName = wrapper.methodStruct.getVariableNamer().renameParameter(wrapper.methodStruct.getAccessFlags(), type, parameterName, index);
              buf.appendVariable(parameterName, true, true, lambdaInfo.content_class_name, lambdaInfo.content_method_name, realDesc, index, parameterName)
                .append(": ")
                .appendTypeName(KTypes.getKotlinType(type), type);
            }

            buf.append(" ->");
          }

          buf.appendLineSeparator();

          if (wrapper.decompileError == null) {
            DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, wrapper);
            RootStatement root = wrapper.root;
            if (root != null) {
              try {
                List<KExitExprent> exits = root.getDummyExit().getPredecessorEdges(StatEdge.TYPE_BREAK).stream()
                  .flatMap(edge -> edge.getSource().getExprents().stream())
                  .filter(expr -> expr instanceof KExitExprent kexpr && kexpr.getExitType() == ExitExprent.Type.RETURN)
                  .map(expr -> (KExitExprent) expr)
                  .toList();

                if (exits.size() > 1) {
                  // Possibly multiple distinct exit points, add a name to this lambda
                  //TODO: figure out a more robust way to do this
                  String lambdaName = name.substring(name.indexOf("$lambda$") + 1).replace("$", "_");
                  buf.prepend(lambdaName + "@");

                  for (KExitExprent exit : exits) {
                    exit.setLambdaName(lambdaName);
                  }
                }

                TextBuffer child = root.toJava(indent + 1);
                child.addBytecodeMapping(root.getDummyExit().bytecode);
                buf.append(child, node.classStruct.qualifiedName, InterpreterUtil.makeUniqueKey(name, lambdaInfo.content_method_descriptor));
              } catch (CancelationManager.CanceledException e) {
                throw e;
              } catch (Throwable t) {
                String message = "Lambda " + name + " " + lambdaInfo.content_method_descriptor + " in class " + node.classStruct.qualifiedName + " couldn't be written.";
                DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN, t);
                wrapper.decompileError = t;
              }
            }
          }

          if (wrapper.decompileError != null) {
            List<String> lines = new ArrayList<>();
            if (DecompilerContext.getOption(IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR)) {
              lines.addAll(ClassWriter.getErrorComment());
              ClassWriter.collectErrorLines(wrapper.decompileError, lines);
            }

            for (String line : lines) {
              buf.appendIndent(indent + 1).append("// ").append(line).appendLineSeparator();
            }
          }

          buf.appendIndent(indent).append("}");

          return buf;
        }
      } finally {
        DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, outerWrapper);
      }
    } else if (isAnonymous()) {
      ClassesProcessor.ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(getNewType().value);
      TextBuffer buf = new TextBuffer();
      new KotlinWriter().writeClass(node, buf, indent);
      return buf;
    }

    return super.toJava(indent);
  }

  @Override
  public Exprent copy() {
    return new KNewExprent((NewExprent) super.copy());
  }
}
