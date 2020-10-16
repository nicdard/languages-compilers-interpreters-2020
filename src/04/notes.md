# Lexing

## Lexical Analysis using ocamllex and menhir
* examples of hand-coded scanners
* tutorial ocamllex + examples

Scanning Goal: for chars stream to tokens stream, semplify the parser job and remove irrelevant details

### Tokens
Described with regular expressions.

### Scanner generators:
Description of tokens as input and the code to run when the pattern is recognised.
* flex -> C
* ocamllex -> OCaml

Those tools suffer the problem that it is difficult to debug the output code. Futhermore you would like to implement a good error recovery or signaling policy that is not possible with those tools.

## Hand coded scanners
* mutable data structure to store the state of the scanner (input buffer, line...)
* a function for each token class of complex token
* a function scanner-main that takes the next char of the input stream (also lookahead if needed) and calls the right sub-routine using a big switch.

### References:

#### For newbies
* Crafting Interpreters: Big tutorial for interpreter (VM with bytecode for the lox language) -> Scanner.java is an example of hand-coded scanner with a map of the keyword, sub-routines for each pattern, a mutable data structure to store the status and a main function with the big switch. [Good newbie resource https://craftinginterpreters.com/].
* Writing compilers and interpreters: Chapter 3 [Good example of Design patterns (example Strategy Pattern)].

#### Real hand-coded scanners:
* Go
* Rust
* Python
* Clang
* Javascript (V8)