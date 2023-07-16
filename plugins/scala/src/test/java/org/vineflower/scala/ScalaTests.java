package org.vineflower.scala;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.util.ArrayList;
import java.util.List;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.SCALA;

public class ScalaTests extends SingleClassesTestBase{
  
  protected void registerAll(){
    registerSet("Entire Classpath", this::registerScalaTests,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.TERNARY_CONDITIONS, "1",
      IFernflowerPreferences.FORCE_JSR_INLINE, "1"
    );
  }
  
  private void registerScalaTests(){
    registerSc("TestCaseClasses", "Option1$", "Option2$", "Option3$", "EnumLike$");
    registerSc("TestObject$");
    registerSc("TestCompanionObject$");
    registerSc("TestDefaultParams$");
    registerSc("TestImplicits", "AddMonoid$", "MulMonoid$", "ConcatMonoid$", "Monoid");
    registerSc("TestAssocTypes", "Shuffler", "IntShuffler$", "StringShuffler$");
  }
  
  private void registerSc(String name, String... extras){
    List<String> args = new ArrayList<>(2 + extras.length * 2);
    if(name.endsWith("$")){
      var without = name.substring(0, name.length() - 1);
      args.add(name);
      name = without;
    }
    for(String extra : extras){
      if(extra.endsWith("$"))
        args.add(extra.substring(0, extra.length() - 1));
      args.add(extra);
    }
    register(SCALA, name, args.toArray(String[]::new));
  }
}
