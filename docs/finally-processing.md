# Finally processing

This document aims to explain how `finally` statements are detected and reconstructed.

## What is a `finally` (in Java)

You might have used a "try finally" statement before, but what does it do actually?

Java 25 spec (14.20. The `try` statement):

> A `try` statement executes a block. If a value is thrown and the `try` statement has one or more `catch` clauses that
> can catch it, then control will be transferred to the first such `catch` clause. If the `try` statement has a
> `finally` clause, then another block of code is executed, no matter whether the `try` block completes normally or
> abruptly, and no matter whether a `catch` clause is first given control.

(This is later followed by a 17-step instruction plan on how to actually evaluate a try finally in section 14.20.2.)

Unless you have experience reading the Java spec, that might not have been a great explanation at all. Let's digest it.
In short: A `try` statement will execute the first block it's given. If that block produces an error, it will look
through its `catch` blocks to see if any of them are able to handle the error. If so, the first one that is applicable
is selected and that block is executed. At the end of the execution of the code inside the `try` and (if applicable)
`catch` block, execution is ALWAYS transferred to the `finally` block. This includes exiting "normally" meaning that
execution has just reached the end of the block, and "abruptly" which means a (yet) uncaught `throw`, a `return`,
a `break`, a `continue` or a `yield` statement have been executed, and it's target (in case of `break`, `continue` and
`yield`) is outside the "try finally".

Now, this is long and complex, but we can reduce this complex logic a bit. Although the spec doesn't mention this, the
following 2 code snippets are essentially equivalent.

```java
try{
  // block 1
  }catch(...){
  // block 2
  }finally{
  // block 3
  }
```

is equivalent to

```java
try{
  try{
  // block 1
  }catch(...){
  // block 2
  }
  }finally{
  // block 3
  }
```

This simplification is used in Vineflower. A "try-catch-finally" is actually a "try-catch" inside the `try` block of
a "try-finally". When the statement tree is converted into java code, the "try-finally" will see that it's `try` block
is just a "try-catch" and will not write its own `try` block, but will instead just append its `finally` block to the
"try-catch".

### Finally control flow hijacking.

a.k.a. Why does my ide yell at me?

Let's look at a simple example

```java
int test(int x) {
  while (true) {
    try {
      // ...
      if (x < 20) {
        return x + 5;
      }
      // ...
    } finally {
      // ...
      continue;
    }
  }
}
```

A first naive thought might be that the `continue` is redundant, after all, it's the last statement executed inside a
while statement. If you open it in a smart editor however, you might get warnings along the lines of:

* "`continue` in `finally`"
* "`finally` can't complete normally"

Instead of a "redundant `continue`. What are these warnings about?

The snippet from the specs copied above is only a short "informative" explanation of what a try-catch-finally does, and
it misses an important part compared to the 17-step normative specification. As mentioned, after the execution of the
`try` block (and maybe a `catch` block), execution is transferred to the `finally` block, but what happens after the
`finally` block? The spec differentiates 2 cases:

* The `finally` block completes "normally" (it just ended, no thrown exception, no `return`, no `break`, `continue` or
  `yield`). In this case, the execution will then go to wherever the `try` block (or `catch` block) wanted to go to.
* The `finally` block completes "abruptly" (it ended by throwing an exception, or running a `return`, `break`,
  `continue` or `yield`). In this case, the execution will not resume where the `try` (or `catch`) block wanted to go
  to, but instead, it will go wherever this "abrupt end" wants us to go to. Method exit in case of `return` or an
  uncaught error, a `catch` block in case of a caught error, the end of a loop in case of `break`, the start of a loop
  in case of a `continue`, the result of a `switch` expression in case of `yield`.

The example code above will thus never `return` and instead run in an infinite loop. It will calculate the result of
`x + 5`, and store it in a temporary variable while it evaluates the `finally` block. But the `finally` block will
always end with the `continue` and thus always restart the loop and ignore the fact that there was a `return`.

This behavior is almost never the intention of the user. This construct can occur in many languages (Java, Python,
JavaScript, ...). Some languages produce warnings, other give compile errors, and those that don't, have linters that
don't allow it.

## How are `finally`s compiled

(This section assumes the Vineflower simplification listed above)

In the initial Java 1 implementations, `finally` blocks were compiled using the `jsr` and `ret` opcodes. This allowed
for the creation of mini-functions (subroutines) inside the larger function. At each point in the `try` block, right
before an exit out of that block (`return`, `break`, ... or just normally exiting) the subroutine with the code of the
`finally` block was called. Additionally, there is an exception handler present that catches all exceptions (VF: "
catch-all") in the code for the `try` block which jumps to a piece of code that stores the exception in a variable, also
jumps to the `finally` subroutine and then rethrows the caught exception.

However, even though there are restrictions on the subroutines (no recursing, each subroutine can have at most one `ret`
instruction, and a nested subroutine call can no longer be returned from if an outer sub routine has already returned),
it made validating and optimizing the bytecode non-trivial. As a result, starting in Java 2-3, and required for classes
with classfile version 51.0 and above (Java 7), `jsr` and `ret` were no longer used and instead, each exit point and
the catch-all will jump to their own copied version of the `finally` block code.

Handling `jsr` and `ret` instructions isn't only annoying for the Java Runtime, it's also annoying for Vineflower.
As a result, one of the very first processing steps on the parsed Control Flow Graph is the inlining of jsr
instructions. This means that we convert old Java 5 and before `finally`s into the version that we'd expect for Java 6+.
Given that in a later step we will need to deduplicate these, this might seem like a weird action to take but abusing
`jsr` and `ret` was really popular in obfuscators. This meant that assuming that these subroutines can always be
converted into "try-finally"'s isn't an option.

## Decompiling `finally`

The decompilation of `finally` happens currently in these 3 steps.

1. Decompile the "try catch" blocks corresponding to the catch-all handler of the finally
2. Validate that the caught exception in the handler behaves correctly, and categorize the finally exits.
3. Find and remove all copy versions.

Each of these 3 steps will give different results if that step fails.

### Decompiling the `catch` block.

Each exception handler in the bytecode has an exception type it catches. However, there is also an option to not
specify a type. In this case, ALL exception are caught. This ends up being equivalent to catching `Throwable` as all
types that can be thrown MUST inherit from `Throwable` in some way. These types of catches are thus called "catch all"

In Java, there is no way to manually specify "catch all", and finally will always use "catch all". As a result, the
original creator of Fernflower (Stiver) decided to use this as a single source of truth of whether a `catch` block is
supposed to remain a `catch` block, or whether it is supposed to be a `finally`. These "catch all" handlers are not
eligible for the standard `CatchStatement` detection. Instead, they are converted into `CatchAllStatements` by the
`DomHelper`. All basic blocks that are only reachable by the `catch` handler will then be put inside the `catch` block.

If anything goes wrong with detecting the `CatchAllStatement`, the decompilation process for the method will be halted
due to a "failure to decompose".

### Validation of the `catch` handler and classification

The next step is `getFinallyInformation`. The instructions are converted into initial exprents and SSA and SSAU analysis
are run. This is used to figure out if the caught exception behaves. In case the catch handler immediately stores the
exception variable inside a variable, this variable is considered the "exception var", otherwise the fake stack var will
be the "exception var". In the case of immediately storing the caught exception, the handler is considered to be a
`STORE` type handler. SSA processing is then used to group all reads and writes of each var into groups based on whether
they are connected in some way. Then all statements are analyzed. There should only be 2 types of accesses to the "
exception var".

The first is the UNIQUE initial assignment at the start of the catch handler. In case of a `STORE` handler that is the
assignment of the real var, in all other cases, it is the virtual assignment to the fake stack var injected at the
start of the handler.

All other accesses MUST be just throwing the exception var. If this is not a `STORE` handler, it must just be
`throw <stack var>`. In case of a `STORE` handler, the process must be in 2 parts, as these are still raw exprents
without any inlining performed. It must be `<stack var> = <real var>`, followed by `throw <stack var>`. Note that the
`throw` can be the next exprent in the basic block, or the first exprent in next basic block (assuming there is only one
single next block).

Each of the basic blocks that end with the later access will be classified as `IMPLICIT_END`, these are the places (or
normally the single place, FIXME: VF assumes there is only 1 and misdecompiles) that (for the non exception case) lead
out of the finally that will continue the execution to the location the `try` block wanted to go to.

The code classifies 2 other types of exits. Those that `return` or `throw` inside the `finally` handler are called
`METHOD_END`. They don't need any other processing so are kept separate. Finally, there is `EXPLICIT_END`. These are
exits that also interrupt the control flow and do not let control flow go to where the `try` block was planning to go.
These are also called `sideExits` in parts of the code.

At the end there is a check to see whether there the catch handler is considered "empty". This analysis seems to be
broken though, and its goal is not clear.

If the analysis results in an error (e.g. the "exception var" is used in an illegal way), the "catch-all" will just
decompile as a "catch Throwable" with a comment `"Could not inline inconsistent finally blocks"`.

### Deduplication of finally handler

The final step is to find all exit points of the `try` block and validate that they go to their own copy of the
`finally` block.

All jumps out of the `try` block are identified, and one by one are compared with the `catch` block. For each so-called
"sample", we start with comparing the start block (sample block) against the first block of the catch handler (pattern
block) and start comparing instructions one by one. If they all(1)(2) match(3), then its successors are compared in the
same way, as long as the pattern successor blocks are still inside the finally catch handler. If not, then that edge is
a type
of "finally exit". Catch handlers on these sample & pattern blocks are also compared. The exception types must match
and the handlers are then also compared if they are inside the finally catch handler.

The caveats:

(1): In case of the very first block of the finally catch handler, an initial `pop` (discarding the exception) or
`astore` (storing the exception) instruction will be skipped in the comparison. Additionally, for the "normal" exit
out of the `finally` block, the `throw` (and previous `aload` in case of a `STORE` finally type) are also skipped.

(2): It is permitted for a sample block to contain more instruction than its corresponding pattern block. In this case,
the remaining unmatched instructions will be split into a new basic block, taking over all the successors, copying the
exception handlers and becoming the sole successor of the original sample block

(3): Matching instructions ignores a few things. First, variables defined inside a `finally` block can have different
variable indices between copies. The processor tries to keep a list of match ups but is actually too lenient. Jump
targets in jump and switch blocks are ignored.

If everything looks okay, then the info is bundled into an `Area` object. If all the jumps out of the `try` block have
been validated, the `Area`s are deleted from the graph, and the exception handling instructions are removed from the
finally catch handler so it can be used as a proper `finally` block instead.

If any of the jumps fails to validate, the `catch` block is still converted into a `finally` block, but a "semaphore"
variable is introduced. It is set to true when the `try` block is entered, and set to false when it is exited. The code
inside the `finally` block is then only executed if the value is set to true. effectively emulating a `catch` block with
a `finally` block. All other "copies" (those that do and those that don't match) are left as is.

### Multiple `finally`'s

After a CatchAllStatement is processed, the result is stored (did the `finally` successfully match or not, if not is
there a semaphore variable or not, and which variable is it). And the dom helper is run again. No attempt is made to
keep working on the original tree.