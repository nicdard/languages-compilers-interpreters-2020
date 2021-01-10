# Representing Values
Many instruction sets store the value directly in the code stream right after the opcode. These are called **immediate instructions** because the bits for the value are immediately after the opcode.

That doesn’t work well for large or variable-sized constants like strings. In a native compiler to machine code, those bigger constants get stored in a separate “constant data” region in the binary executable. Then, the instruction to load a constant has an address or offset pointing to where the value is stored in that section.

# Dispatching instruction optimisations tecniques
Given a numeric opcode, we need to get to the right C code that implements that instruction’s semantics.
* direct threaded code
* jump table
* computed goto

# Parsing
Vaughan Pratt’s “top-down operator precedence parsing”: http://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy/

# Type system
* statically typed
* dinamically typed
* unityped

# Comparisons
Is a <= b always the same as !(a > b)? According to IEEE 754, all comparison operators return false when an operand is NaN. That means NaN <= 1 is false and NaN > 1 is also false. But our desugaring assumes the latter is always the negation of the former.

# Stack based VM: statement vs expressions
 Statements are required to have zero stack effect, after one statement is finished executing, the stack should be as tall as it was before.

 Expressions produce and consume elements of the stack.

# Emittig byte code for jumps
 When we’re writing the OP_JUMP_IF_FALSE instruction’s operand, how do we know how far to jump? We haven’t compiled the then branch yet, so we don’t know how much bytecode it contains.
 
To fix that, we use a classic trick called backpatching. We emit the jump instruction first with a placeholder offset operand. We keep track of where that half-finished instruction is. Next, we compile the then body. Once that’s done, we know how far to jump. So we go back and replace that placeholder offset with the real one now that we can calculate it. Sort of like sewing a patch onto the existing fabric of the compiled code.

# Continuations
Many Lisp implementations dynamically allocate stack frames because it simplifies implementing continuations. If your language supports continuations, then function calls do not always have stack semantics. https://en.wikipedia.org/wiki/Continuation
