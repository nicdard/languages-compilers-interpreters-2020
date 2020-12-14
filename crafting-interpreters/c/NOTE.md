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
