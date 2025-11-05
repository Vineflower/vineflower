# Architecture
This file provides information on how Vineflower is structured, with descriptions of how each part of the codebase works.

## Terms
Vineflower uses a large amount of terms and shorthand names within its code, for which explanations are provided here.
The first name is the full name and the names in parentheses are the shorthand names that are used throughout the code.
* Exprent (expr, exp) Refers to an expression that Vineflower uses to create the java code, such as a constant or method invocation. Exprents are stored in a tree structure.
* Statement (stat) Refers to a statement within the code used to structure the code, such as an if statement or a loop. Statements are stored in a graph structure.

## The decompilation process
When a method gets decompiled, it passes through multiple stages through which it is converted from bytecode to java code.
At a high level, this process consists of bytecode reading, instruction sequencing, control flow analysis, code structuring, and java writing.
Each of these steps will be outlined in detail below.

### Bytecode reading and instruction sequencing
This is the simplest part of the process, as it just reads the bytecode out of the code struct and initializing them. This takes place in `StructMethod#parseBytecode`.
The bytecode is placed in a big instruction sequence, which is an intermediate representation of the bytecode in an internal format.

### Control flow graph analysis
As soon as the instruction sequence is created, the control flow graph is created. This transforms the graph from a linear sequence of instructions to a graph of basic blocks.
This step happens in `ControlFlowGraph#<init>`. The graph splits on jumps, branches, and returns to turn the instructions into a graph, with edges denoting control flow.
After the control flow graph is parsed, a couple of transforms are applied onto it. These transforms include parsing JSR instructions, removing empty blocks and redundant GOTOs,
and a few try-catch/synchronized fixes for strange or malformed exception ranges. After this is done, basic blocks are merged to create a compact and simplified graph.
This rewires the blocks to ensure that the only jumps left are the ones that are necessary. This is done in `MethodProcessor.codeToJava`, near the top.

### Control flow to structured statement
As Java code must be structured flow (i.e. no goto statements) we must decompose the control flow graph, which contains only arbitrary flow into a graph that contains jumps in terms of breaks, continues, and fallthrough.
Since a single control flow graph can be expressed in multiple structured flows, we have our work cut out for us in order to choose the most simple form. This step occurs in `DomHelper` and `FastExtendedPostdominanceHelper`.
First, the basic blocks are copied over into statements and are placed into statement graphs. Then, complex statements (such as if statements and loops) are identified from the basic blocks.
This has the effect of building the statement graph from the inside-out, leading to more complex statements being built out of more simple ones. Take, for example, a simple while loop that goes like this:
```java
while (i > 0) {
    i--;
}
```
This is parsed as so:
```java
while (true) {
    if (!(i > 0)) {
        break;
    }

    i--;
    continue;
}
```
As the if statement is parsed first, followed by a sequence statement to incorporate the i--;, and then the loop statement.
When statements are transformed and incorporated, the edge types are refined into more specific types such as breaks and continues.
However, this step isn't always possible when there is complex control flow, for example nested loops with breaks and continues. If the statement transformer isn't able to identify all the high level statements in the graph, `DomHelper` will
split the graph into a subgraph and try to iterate on that first. This recursive descent behavior allows the statement parser to work on simpler parts of the graph, and then use those simple inner subgraphs to help identify the outer statements.
If the recursive descent completes and the statement is still unable to be parsed, this means that either the control flow is irreducible, the graph is malformed, or the parser is unable to discover the postdominance of the control flow graph.
In any of these cases, the parser will attempt to split irreducible code and if that fails it will throw a parsing error.
This type of processing creates *functionally correct* code in almost all cases, but it's not readable for the most part. After the initial statement parsing, many more steps exist to iterate on and refine the statement structure.

### Types of statements
Before going forward, it's useful to know the types of statements and the order they are discovered when parsing the control flow graph.
* `DoStatement`: Discovered first. This denotes a loop, which can be an infinite, while, for, for-each, or do-while loop. When statements are first parsed they are always infinite, and the type is refined in later steps.
* `SwitchStatement`: Discovered second. This denotes a switch statement. An interesting thing to note is that sometimes due to parsing order some code outside the switch originally can be incorporated into the default case, but the control flow should be identical.
* `IfStatement`: Discovered third. This denotes an if statement, which can be a simple if or and if-else. For boolean and (&&) and boolean or (||) expressions, each part of the condition is split into its own if statement and combined later, as that is how it's defined in the bytcode.
* `SequenceStatement`: Discovered fourth. This denotes a grouping of statements executed in order, and are used to make a list of statements adjacent to each other.
* `CatchStatement`: Discovered fifth. This denotes a try-catch statement, which can either be a regular try or a try-with-resources. When statements are first parsed they are always regular try, and become try-with-resources in later steps.
* `CatchAllStatement`: Discovered last. This is a special kind of statement, as it can either represent a parsed finally, an unparsed finally, or a parsed finally with a predicated entry. When statements are parsed they are always unparsed finally, and the finally processor modifies them as needed. We will go in depth into finally processing in the next section.
#### Other statements
These statements aren't discovered in the parser, but they are present and appear in the statement graph in other ways.
* `BasicBlockStatement`: Represents a block of expressions within the statement graph. These are created initially when the control flow graph is copied into an initial statement graph.
* `RootStatement`: Represents the topmost statement in the statement graph. This is the only statement that doesn't have a parent.
* `DummyExitStatement`: Represents the exitpoint of a method. Since there is only one exit point, all other returns will have an edge to this statement.
* `GeneralStatement`: This is a temporary holder for unparsed or partially parsed statements in the initial graph parsing. After the parsing is complete, these statements are removed and replaced with the correct statement type.
* `SynchronizedStatement`: Represents a synchronized block. Because these have special control flow requirements, they are identified after the initial parsing is complete.

### Finally block parsing
Finally statements in java are peculiar because the compiler duplicates the finally block and places it at the end of each exit from the try block and catch blocks. It's the job of the decompiler to identify these duplicates and collapse them into a structured finally block.
This is done in `FinallyProcessor#iterateGraph`. The first step is to identify the `CatchAllStatement`s, and get their finally handlers. After that, the exit points of the try body is found and the code is checked for duplicates.
If the code is an exact duplicate in all the spots, the duplicates are removed and the finally block is correctly decompiled. If the code is not an exact duplicate, the finally block is marked as a predicated finally block, which contains an if statement at it's entry.
The if statement uses a synthetic boolean variable as it's condition, with the boolean being set to true at the start of the try and false at any exit in the try. The finally block will only execute if the boolean is true, i.e. the try block didn't end normally.
After any single finally block is identified, the control flow graph is parsed again via `DomHelper` to ensure the statement graph reflects the deduplication of the finally blocks.

### Expression parsing
Now that the structure of the statement has been identified, we can start parsing the expressions that comprise the actual bytecode. This is first parsed in `ExprProcessor`. After this stage, the expressions are parsed into assignments based on the stack content.
For example,
```java
System.out.println("Hello world!");
int i = 0;
i++;
return i;
```
is parsed as
```java
var10000 = System.out;
var10001 = "Hello world!";
var10000.println(var10001);
var10002 = 0;
i = var10002;
var10003 = 1;
i = i + var10003;
var10004 = i;
return var10004;
```
The initial parsing is exceptionally hard to read, but it ensures that the stack contents are simulated properly with assignments to temporary variables. All variables named "var10000" and similar are simulated stack variables.
After the initial parsing of expressions, the next task is to collapse these expressions into a more readable form. This is done in `StackVarsProcessor#simplifyStackVars`.
The idea is that when you have code such as `var10000 = i; return var10000;`, you can collapse the assignment and remove the simulated stack variable. In order to calculate which variables can be collapsed,
two different views of the expressions are used. The first is "SSA form" (Static Single Assignment form), created in `SSAConstructorSparseEx`. SSA-form gives each assignment to a variable a unique "version", allowing the decompiler to track what value a variable holds at any point in it's lifetime.
SSA-form is used to determine variable identities for single peephole optimizations, which occur in `SimplifyExprentsHelper`. After these are done, the SSA-form is reverted (all versions are reset to 0)
and the next form, SSAU-form, is created. SSAU-form (Static single Assignment-Usage form) is similar to SSA-form, but it also increments the version on each usage of a variable. This allows the creation of a graph of variable usages and assignments,
which is then used for the automatic translation and reduction of expressions. This is done in `StackVarsProcessor#iterateExprent`. After a round of simplification has passed, the SSAU-form is reverted and the loop continues.
The stack var simplification runs until no more stack vars can be simplified.

### Further processing
After the stack vars have been simplified into proper expressions, the main processing loop begins. In the main loop, a wide array of processing steps occur to transform the statements and expressions into more readable and ideal forms.
These steps are done in `MethodProcessor.codeToJava`. Each processing step is different and does a different task, and there are too many to list here so only the most important steps are documented below.
First, loops are restructured. This is done in `MergeHelper`. This step takes the infinite loops identified by the statement parser and tries to identify exit points so that the loop can be narrowed into a while, for, foreach, or do-while loop.
Restructuring steps are also taken to move if statement bodies to the outside of the loop, to help identify loop exitpoints. After that, labels are identified and simplified. This is done in `LabelHelper`.
Before this step, all breaks and continues are labeled, to make sure all jumps are explicit in where they go. Since specific labels aren't needed for breaks or continues out of a single statement, they can be simplified to not need the label.
Similarly, breaks and continues that are meaningless (i.e. continues at the end of a loop) are made non explicit, and such are invisible in the decompiled output. This allows the decompiler to know which jumps are needed for certain and which jumps are not needed at all.
Then, try with resources is attempted to be identified. This happens in `TryWithResourcesProcessor`, which tries to identify resource variables and exit points, to collapse them into a try with resources statement similar to finally processing.
Inline single blocks is another important step, as it takes the existing blocks and tries to move labeled break jumps into the place where the labeled jump is meant to go. This cleans up most of the artifacts left behind by the
initial statement parsing. This happens in `InlineSingleBlocksHelper`. The final step that will be described here is variable definition processing, which only happens at the end, in `VarDefinitionHelper`.
Variables are analyzed to find their first usage and scope, and are marked as a definition. After this, a few other minor steps are done to cleanup and improve the code, and then it's ready to be written.

### Java writing
`ClassWriter` takes the java code, in Statements and Exprents, and writes them to a string buffer. It traverses down the class structure, and writes all of the fields and methods that it contains. The method writing starts with calling
`toJava` on the root statement, which calls toJava on the statement it holds, and this recurses until the entire statement graph and exprent tree is written.