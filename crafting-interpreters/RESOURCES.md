https://craftinginterpreters.com/a-map-of-the-territory.html
# Compiler-compiler
* Yacc
* Lex
* Bison

# Intermediate Rapresentation
* “control flow graph” 
* “static single-assignment” 
* “continuation-passing style” [see also: Kotlin coroutines]
* “three-address code” [ GIMPLE ]
    * RTL

# Optimisation
* *costant-folding*: if some expression always evaluates to the exact same value, we can do the evaluation at compile time and replace the code for the expression with its result

The basic principle here is that the farther down the pipeline you push the architecture-specific work, the more of the earlier phases you can share across architectures.

# Runtime
* Fully compiled languages -> the runtime gets inserted directly into the resulting executable
* Virtual Machine -> the VM is also the runtime for the language

# Single-pass compiler
Some simple compilers interleave parsing, analysis, and code generation so that they produce output code directly in the parser, without ever allocating any syntax trees or other IRs. These single-pass compilers restrict the design of the language. You have no intermediate data structures to store global information about the program, and you don’t revisit any previously parsed part of the code. That means as soon as you see some expression, you need to know enough to correctly compile it. Examples: C, Pascal!

## Syntax-directed translation
Associate an action with each production of the grammar, usually this action generates output code.  Then, whenever the parser matches that chunk of syntax
it executes the action, building up the target code one rule at a time.

# Tree-walk interpreters
Source -> AST -> interpreter traverse it. Slow and usually not used in practice.

# Transpilers / Source-to-source compilers
Reuse existing source languages as intermediate representation of your new language to save work.
* Almost every language can be compiled to js: 
https://github.com/jashkenas/coffeescript/wiki/list-of-languages-that-compile-to-js
* find out more on web browsers languages: https://github.com/webassembly/

# Just in time compilation (JIT)
On the end user’s machine, when the program is loaded—either from source in the case of JS, or platform-independent bytecode for the JVM and CLR—you compile it to native for the architecture their computer supports. The most sophisticated JITs insert **profiling hooks** into the generated code to see which regions are most performance critical and what kind of data is flowing through them. Then, over time, they will automatically recompile those hot spots with more advanced optimizations.
