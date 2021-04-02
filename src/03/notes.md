# Modules and Compilation

Module: each source file is a Module and everything is public by default. 
Generally, the filename starts with a lowercase.
The module name starts with uppercase.

Dot notation -> Used to qualify a name: Module.name

Module alias: module Alias_name = Module_name;

We can also open a Module, importing all the public members of the module (so we can refer to them without qualifing the name) but this can lead to name clashes. 

A Module create also a namespace, and it can also be parametric, taking a module as parameter.

## Compilation:
ocmalc -c filename.ml.
The compiler generates two files:
* .cmi: the module interface -> what is public in the module.
* .cmo: the module implementation

We can then compile the final program calling:
ocamlc -o ex filename1.cmo filename2.cmo -> produces an executable ex.  

## Separating interface and implementation
We can also provide a .mli file explicitely providing so the public interface of a module. This is considered a good practice, we can thus provide to the user of the module an abstract data type, encapsulating the implementation and hiding the implementation. **Module Signature**.

## Functor
A module that is parametrized on another module (module type).
OCaml can check that the actual module paramter has the correct signature (expected by the functor).

# Exception
There is no hierarchy, it is just an algebraic data type.
Warning: prefer Option type to Exception.

# Standard Library Modules
## Map
It is implemented as an immutable balanced tree (takes O(log(n)) for insertions and searching). It is a Functor, we have to instantiate it for the key with another Module. It is parametric on the type of the values.

To instatiate the Map we can use the internal Make module (the functor), which takes a totally ordered type (just a module with a compare function).

Example: The String module is an OrderedType (it has the compare function) so we can create a StringMap:
module StringMap = Map.Make(String);; This creates a new module StringMap which is a map from string to a polymorphic type t.

## Inline Modules
We can define modules inline using the syntax: struct ... end.

# Imperative Features
* ";": sequencing operator, it expects that the left side expression is an unit type and returns the right side expression. You can convert whatever value the lse has to unit using the ignore function.

* let name = ref(value): declare references (modifiables identifiers). A reference is a particular Record type with one only field contents and the ":=" operator which can modify the content of a reference. (:=) : 'a ref -> 'a -> unit = <fun\>. A ref is allocated on the heap (!= from F# mutable which is allocated on the stack).

* Refs also provide us with the "!" operator, which take a ref and gives us its content.

# Arrays
* syntax: [| el1; el2; ... |]
* random access operator: array.(index)

# Hash Table
The imperative version of Map. The problem is that an Hash Table uses an hash function, so the Hashtbl.Make functor expects to receive a module providing an equal and an hash function. 

# Dune - build system
https://medium.com/@bobbypriambodo/starting-an-ocaml-app-project-using-dune-d4f74e291de8
