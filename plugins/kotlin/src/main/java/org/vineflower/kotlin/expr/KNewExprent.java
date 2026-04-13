package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.main.ClassWriter;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.decompiler.CancelationManager;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.vineflower.kotlin.KotlinChooser;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.struct.KClass;
import org.vineflower.kotlin.struct.KElement;
import org.vineflower.kotlin.struct.KFunctionReference;
import org.vineflower.kotlin.util.KTypes;

import java.util.ArrayList;
import java.util.List;

public class KNewExprent extends NewExprent implements KExprent {
  private static final String REFERENCE_CTOR_DESC = "(Ljava/lang/Object;)V";
  private boolean inAnnotation;

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
    TextBuffer buf = new TextBuffer();
    ClassesProcessor.ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(getNewType().value);
    if (node != null && new KotlinChooser().isLanguage(node.classStruct)) {
      KElement ktData = node.classStruct.getAttribute(KElement.KEY);
      if (ktData instanceof KFunctionReference) {
        Exprent receiver = getConstructor().getLstParameters().get(0);
        TextBuffer ref = KotlinWriter.stringifyReference(indent, node, bytecode, receiver);
        if (ref != null) {
          return ref;
        }
      }

      if (node.type == ClassesProcessor.ClassNode.Type.LOCAL && ktData instanceof KClass) {
        // Work around the Java-targeted anonymous class verification
        node.type = ClassesProcessor.ClassNode.Type.ANONYMOUS;
        buf.addBytecodeMapping(bytecode);
        new KotlinWriter().writeClass(node, buf, indent);
        return buf;
      }
    }

    if (isLambda()) {
      MethodWrapper outerWrapper = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
      try {
        ClassesProcessor.ClassNode.LambdaInformation lambdaInfo = node.lambdaInformation;
        String name = lambdaInfo.content_method_name;
        if (name.contains("$lambda$")) {
          //TODO perform more exhaustive checks
          MethodDescriptor referencedDesc = MethodDescriptor.parseDescriptor(lambdaInfo.method_descriptor);
          MethodDescriptor realDesc = MethodDescriptor.parseDescriptor(lambdaInfo.content_method_descriptor);

          MethodWrapper wrapper = node.getWrapper().getMethodWrapper(name, lambdaInfo.content_method_descriptor);
          buf.appendPunctuation("{").appendWhitespace(" ");

          if (referencedDesc.params.length > 0) {
            int index = lambdaInfo.is_content_method_static ? 0 : 1;
            int startIndex = realDesc.params.length - referencedDesc.params.length;
            for (int i = 0; i < realDesc.params.length; i++) {
              if (i < startIndex) {
                index += realDesc.params[i].stackSize;
                continue;
              } else if (i > startIndex) {
                buf.appendPunctuation(",").appendWhitespace(" ");
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
                .appendPunctuation(":").appendWhitespace(" ")
                .appendTypeName(KTypes.getKotlinType(type), type);

              index += type.stackSize;
            }

            buf.appendWhitespace(" ").appendOperator("->");
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
                  buf.prepend(lambdaName + "@", TokenType.LABEL);

                  for (KExitExprent exit : exits) {
                    exit.setLambdaName(lambdaName);
                  }
                } else {
                  exits.get(0).setLambdaName(null);
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
              buf.appendIndent(indent + 1).appendComment("// " + line).appendLineSeparator();
            }
          }

          buf.appendIndent(indent).appendPunctuation("}");

          return buf;
        }
      } finally {
        DecompilerContext.setProperty(DecompilerContext.CURRENT_METHOD_WRAPPER, outerWrapper);
      }
    } else if (isAnonymous()) {
      buf.addBytecodeMapping(bytecode);
      new KotlinWriter().writeClass(node, buf, indent);
      return buf;
    }

    if (getNewType().arrayDim == 0) {
      if (!isEnumConst()) {
        String typeName = KTypes.getKotlinType(getNewType());
        
        if (getConstructor() != null) {
          TextBuffer enclosing = getQualifiedNewInstance(getNewType().value, getConstructor().getLstParameters(), indent);
          if (enclosing != null) {
            buf.append(enclosing).appendPunctuation('.');
            ClassesProcessor.ClassNode newNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(getNewType().value);
            typeName = newNode != null ? newNode.simpleName : typeName.substring(typeName.lastIndexOf('.') + 1);
          }
        }

        buf.appendTypeName(typeName, getNewType());
      }
      
      if (getConstructor() != null) {
        if (!isEnumConst() || getConstructor().getLstParameters().size() > 2) {
          appendParameters(buf, getConstructor().getGenericArgs());
          buf.appendPunctuation("(").append(getConstructor().appendParamList(indent)).appendPunctuation(')');
        }
      }
    } else if (isVarArgParam()) {
      boolean first = true;
      for (Exprent element : getLstArrayElements()) {
        if (!first) {
          buf.appendPunctuation(",").appendPossibleNewline(" ");
        }
        buf.append(element.toJava(indent));
        first = false;
      }
    } else if (inAnnotation) {
      buf.appendPunctuation('.');
      buf.pushNewlineGroup(indent, 1);
      buf.appendPossibleNewline();
      boolean first = true;
      for (Exprent element : getLstArrayElements()) {
        if (!first) {
          buf.appendPunctuation(",").appendPossibleNewline(" ");
        }
        if (element instanceof KNewExprent knew) {
          knew.setInAnnotation(true);
        }
        buf.append(element.toJava(indent));
        first = false;
      }
      buf.appendPossibleNewline("", true);
      buf.popNewlineGroup();
      buf.appendPunctuation('.');
    } else if (getLstArrayElements().isEmpty()) {
      //TODO there should never be a multi-dimension new array created here - check if the handling is necessary
      for (int i = 0; i < getNewType().arrayDim - 1; i++) {
        buf.appendMethod("Array", false, "kotlin/Array", "<init>", "(ILkotlin/jvm/functions/Function1;)V").appendPunctuation("(");
        Exprent dim = getLstDims().get(i);
        if (dim.type == Type.CONST) {
          ((ConstExprent) dim).adjustConstType(VarType.VARTYPE_INT);
        }
        buf.append(dim.toJava(indent));
        buf.appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline(" ");
      }

      String returnType = arrayNameForType(getNewType().resizeArrayDim(0).type);

      String methodName = returnType.equals("kotlin/Array") ? "arrayOfNulls" : "<init>";
      String containingClass = returnType.equals("kotlin/Array") ? "kotlin/LibraryKt" : "kotlin/" + returnType;
      String desc = "(I)L" + returnType + ";";
      String text = "<init>".equals(methodName) ? returnType : methodName;

      buf.appendMethod(text, false, containingClass, methodName, desc).appendPunctuation('(');

      Exprent dim = getLstDims().get(getNewType().arrayDim - 1);
      if (dim.type == Type.CONST) {
        ((ConstExprent) dim).adjustConstType(VarType.VARTYPE_INT);
      }
      buf.append(dim.toJava(indent)).appendPunctuation(')');

      for (int i = 0; i < getNewType().arrayDim - 1; i++) {
        buf.appendPossibleNewline(" ", true);
        buf.popNewlineGroup();
        buf.appendPunctuation('}');
      }
    } else {
      CodeType elementType = getNewType().decreaseArrayDim().type;
      String method = switch (elementType) {
        case BOOLEAN -> "booleanArrayOf";
        case BYTE -> "byteArrayOf";
        case SHORT -> "shortArrayOf";
        case CHAR -> "charArrayOf";
        case INT -> "intArrayOf";
        case FLOAT -> "floatArrayOf";
        case LONG -> "longArrayOf";
        case DOUBLE -> "doubleArrayOf";
        default -> "arrayOf";
      };
      String arrayDesc = switch (elementType) {
        case BOOLEAN -> "[Z";
        case BYTE -> "[B";
        case SHORT -> "[S";
        case CHAR -> "[C";
        case INT -> "[I";
        case FLOAT -> "[F";
        case LONG -> "[J";
        case DOUBLE -> "[D";
        default -> "[Ljava/lang/Object;";
      };
      String arrayClass = arrayNameForType(elementType);

      buf.appendMethod(method, false, "kotlin/LibraryKt", method, "(" + arrayDesc + ")L" + arrayClass + ";");
      buf.appendPunctuation('(');
      buf.pushNewlineGroup(indent, 1);
      buf.appendPossibleNewline();
      boolean first = true;
      for (Exprent element : getLstArrayElements()) {
        if (!first) {
          buf.appendPunctuation(",").appendPossibleNewline(" ");
        }
        buf.append(element.toJava(indent));
        first = false;
      }
      buf.appendPossibleNewline("", true);
      buf.popNewlineGroup();
      buf.appendPunctuation(')');
    }

    return buf;
  }

  private static String arrayNameForType(CodeType codeType) {
    return "kotlin/" + switch (codeType) {
      case BOOLEAN -> "BooleanArray";
      case BYTE -> "ByteArray";
      case SHORT -> "ShortArray";
      case CHAR -> "CharArray";
      case INT -> "IntArray";
      case FLOAT -> "FloatArray";
      case LONG -> "LongArray";
      case DOUBLE -> "DoubleArray";
      default -> "Array";
    };
  }

  @Override
  public Exprent copy() {
    return new KNewExprent((NewExprent) super.copy());
  }

  public void setInAnnotation(boolean inAnnotation) {
    this.inAnnotation = inAnnotation;
  }
}
