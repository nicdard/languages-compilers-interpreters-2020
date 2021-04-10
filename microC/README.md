# MicroC

MicroC is a subset of the language C where some simplification are made:

* It supports only integers (`int`), characters (`char`) and booleans (`bool`) as scalar values, array and pointers as compound data types;
* There are no structures, unions, doubles, function pointer;
* No dynamic allocation of memory;
* No multi-dimensional arrays;
* No shorthand for initialize variable during declaration;
* Functions can only return `void`, `int`, `bool`, `char`;
* No pointer arithmetic;
* Pointers and arrays are not interchangeable;
* no separate compilation, all the code of a program must stay in a unique compilation unit;
* there are only two library functions

```C
void print(int)  // it outputs an integer to standard output
int getint()     // it inputs an integer from standard input 
```

# MicroC Parser

The parser is written using `ocamllex` for the scanner and `menhir` for the parser.


### Lexical elements

* Identifiers starts with a letter or an underscore and then can contain letters, underscore and numbers, e.g., `i`, `_local_var`, `string_of_int32`;
* Integer literals are sequence of digits (integers are 32bit values), e.g., `32`, `1024`, `3232`;
* Character literals have the form `'c'` where c is a character, e.g., `a`, `A`, `1`;
* Boolean literals are `true` and `false`;
* Keywords are: `if`, `return`, `else`, `for`, `while`, `int`, `char`, `void`, `NULL`, `bool`;
* Operators are: &,  +, -, *, /, %,  =, ==, !=, <, <=, >, >=, &&, ||, !
* Other symbols: (, ), {, }, [, ], &, ;
* Comments:
    * `//...` single line comments;
    * `/* ... */` multi line comments.

The operators have the following precedence and associativity:

    right    =             /* lowest precedence */
    left     ||
    left     &&
    left     ==  != 
    nonassoc >  <  >=  <=
    left     +  - 
    left     *  /  %
    nonassoc !  &
    nonassoc [             /* highest precedence  */

### Syntax

Here is an ambiguous grammar for MicroC where tokens with no semantic values are enclosed between quotes, e.g., `"("`, whereas tokens with semantic values are capitalized, e.g., `INT`. 
As usual the operator `*` means zero or more occurrences, `+` means one or more occurrences, and `?` means at most one.

    Program ::= Topdecl* EOF
    
    Topdecl ::= Vardecl ";"  | Fundecl
    
    Vardecl ::= Typ Vardesc
    
    Vardesc ::= ID | "*" Vardesc | "(" Vardesc ")" | Vardesc "[" "]" | Vardesc "[" INT "]" 
    
    Fundecl ::= Typ ID "("((Vardecl ",")* Vardecl)? ")" Block
    
    Block ::= "{" (Stmt | Vardecl ";")* "}"
    
    Typ ::= "int" | "char" | "void" | "bool" 
    
    Stmt ::= "return" Expr ";" | Expr ";" | Block | "while" "(" Expr ")" Block 
        |    "for" "(" Expr? ";" Expr? ";" Expr? ")" Block
        |    "if" "(" Expr ")" Stmt "else" Stmt  | "if" "(" Expr ")" Stmt

    Expr ::= RExpr | LExpr

    LExpr ::= ID | "(" LExpr ")" | "*" LExpr | "*" AExpr | LExpr "[" Expr "]"

    RExpr ::= AExpr | ID "(" ((Expr ",")* Expr)? ")" | LExpr "=" Expr | "!" Expr 
        |  "-" Expr | Expr BinOp Expr 

    BinOp ::= "+" | "-" | "*" | "%" | "/" | "&&" | "||" | "<" | ">" | "<=" | ">=" | "==" | "!="

    AExpr ::= INT | CHAR | BOOL | "NULL" | "(" RExpr ")" | "&" LExpr

## Requirement to build the code
The code requires:
* OCaml >= 4.10.1
* Menhir >= 20200624
* ppx_deriving >= 4.5 

You can install the required dependencies via `opam`
```sh
$ opam install menhir ppx_deriving
```
[Here](https://github.com/ocaml-ppx/ppx_deriving), you can the documentation of `ppx_deriving`.

## Building the code
Typing `make` will generate a `microcc.native` executable:
```
$ make
```

To clean-up the folder, run:
```
$ make clean
```

## Directory structure #

Here is a description of content of the repository

    src/                               <-- The source code lives here
    Makefile                           <-- Driver for `make` (uses OCB)
    _tags                              <-- OCamlBuild configuration
    tests/                             <-- Some programs to test your implementation

## The source code

The `src/` directory provides:

    ast.ml                       <-- Definition of the abstract syntax tree of MicroC 
    microcc.ml                   <-- The file from which build the executable 
    parser_engine.ml             <-- Module that interact with the parser
    util.ml                      <-- Utility module  
    parser.mly                   <-- Menhir specification of the grammar 
    scanner.mll                  <-- ocamllex specification of the scanner