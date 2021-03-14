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

# Native Functions
The reason we need new machinery is because, from the implementation’s perspective, native functions are different from Lox functions. When they are called, they don’t push a CallFrame, because there’s no bytecode code for that frame to point to. They have no bytecode chunk. Instead, they somehow reference a piece of native C code.

# Closure implementations
Search for “closure conversion” or “lambda lifting”.

**Upvalue**: a level of indirection, refers to a local variable in an enclosing function. Every closure maintains an array of upvalues, one for each surrounding local variable that the closure uses.

* open upvalue: refer to an upvalue that points to a local variable still on the stack;
* closed upvalue: when a variable which is enclosed in a closure moves to the heap.

# Garbadge Collectors

References: The Garbage Collection Handbook: The Art of Automatic Memory Management brings together a wealth of knowledge gathered by automatic memory management researchers and developers over the past fifty years.

**Conservative GC** is a special kind of collector that considers any piece of memory to be a pointer if the value in there looks like it could be an address.
**Precise GC** knows exactly which words in memory are pointers and which store other kinds of values like numbers or strings.

A value is reachable if there is some way for a user program to reference it, otherwise, it is unreachable.
A **root** is any object that the VM can reach directly without going through a reference in some other object

## Mark-and-seep
Mark-sweep works in two phases:
1. Marking: We start with the roots and traverse or trace through all of the objects those roots refer to. This is a classic graph traversal of all of the reachable objects. Each time we visit an object, we mark it in some way. (Implementations differ in how they record the mark.)
2. Sweeping: Once the mark phase completes, every reachable object in the heap has been marked. That means any unmarked object is unreachable and ripe for reclamation. We go through all the unmarked objects and free each one.

## Metrics
* **Throughput** is the total fraction of time spent running user code versus doing garbage collection work.
* **Latency** is the longest continuous chunk of time where the user’s program is completely paused while garbage collection happens. It’s a measure of how “chunky” the collector is.