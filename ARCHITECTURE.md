# Architecture
This file provides information on how FernFlower is structured, with descriptions of how each part of the codebase works.

## Terms
FernFlower uses a large amount of terms and shorthand names within its code, for which explanations are provided here.
The first name is the full name and the names in parentheses are the shorthand names that are used throughout the code.
* Exprent (expr, exp) Refers to an expression that FernFlower uses to create the java code, such as a constant or method invocation.
* Statement (stat) Refers to a statement within the code used to structure the code, such as an if statement or a loop.

## The decompilation process
When a method gets decompiled, it passes through multiple stages through which it is converted from bytecode to java code.
At a high level, this process consists of bytecode reading, instruction sequencing, control flow analysis, code structuring, and java writing.
Each of these steps will be outlined in detail below.

### Bytecode reading and instruction sequencing
This is the simplest part of the process, as it just reads the bytecode bytes out of the code struct and initializing them. This takes place in `StructMethod#parseBytecode`.

### Control Flow Analysis
Control flow analysis is where the bytecode is analyzed into basic blocks, SSA is run, and the dominator tree is generated to help with analysis and code inference.
Most of this processing happens in `MethodProcessorRunnable.codeToJava`.

### Code structuring
Right after the control flow analysis, the basic blocks are turned into structured code within the same `MethodProcessorRunnable.codeToJava` method. This creates an intermediate
representation of the method that can be then turned into java code.

### Java writing
`ClassWriter#methodToJava` takes the intermediate representation (comprised of statements and exprents) and structures that into java code which is then written.