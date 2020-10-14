# The Fun language
We implement an interpreter for the language without translating the language: we navigate the AST of the language while interpreting it.

This approach is more and more used in DSL.

Generally for general purpose languages we use a mixed approach, which compiles the high level language to a bytecode and then interprets it with a VM.

Interpreter:
* store data;
* track symbols;
* execute instructions.

The interpreter is strongly related to the language's semantic. However the semantic of a language is really a form of interpreter beacuse it provides us a way to execute the instructions.

## Types:
integers, booleans, functions (recursive), conditionals and identifier.

## Environment:
There is no side-effect, so we will just use a map from variables to values.

## Procedure
We will recursively walk the AST to execute the each contruct. Thus, the interpreter is just a bid-step semantic implementation.

# Syntax
## Math
Id: identifiers

Num: integer syntax category

Exp: 
* Num 
* Id 
* if e1 then e2 else e3
* e1 op e2 
* let x = e1 in e2 
* fun f x = e1 in e2 (function declaration, only one parameter) 
* e1 e2 (function application)

## OCaml
One to one translation:
```OCaml
type expr =
    | CstI of Int
    | Var of string
    | If of expr * expr * expr
    | Prim of string * expr * expr (* primitive operator *)
    | Let of string * expr * expr
    | LetFun of string * string * expr * expr
    | Call of expr * expr
```

# Semantic domains
We want the value notion: we have only integers and functionals types. Semantic domain: 
* v in Value = Z U Closure : expressible and denotable values
* p in Env = Id -> Value : environments
* c in Closure = Id x Id x Exp x Env : closures

A closure is a data structure which maintain the information about the function declaration: function name, parameter name, body, environment (we have static scope). In the body function the parameter can occour freely, but we must differenciate it from the rest of non-local identifiers beacuse the parameter should be binded at call-time.

```OCaml 
type 'v env = (string * 'v) list
...
```

## Big step semantic
We want to define a relation on environment values and expressions (on deterministic languages it is just a function). We define a set of inference rules.

```OCaml
let rec eval (e:expr) (env: value env): value =
    (* Here the patterns are the inference rules *)
    match e with
    | CstI n -> Int n (* it goes from syntax to semantic domain *)
    | Var x -> lookup env x
    ...
```

