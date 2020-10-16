# The com.lox.Lox Language
* C family syntax
* scoping approach similar to Scheme
* dynamic typing -> type errors at runtime
* automatic memory management: reference counting and tracing garbage collection
* data-types:
    * Booleans
    * Numbers (double precision)
    * Strings (double quoted)
    * Nil
* expressions:
    * arithmetic
    * comparison and equality
    * logical: The reason and and or are like control flow structures is because they short-circuit. Not only does and return the left operand if it is false, it doesn’t even evaluate the right one in that case. Conversely, (“contrapositively”?) if the left operand of an or is true, the right is skipped.
    * fun application
* statements:
    * print
    * block: { }
    * statement-hood: "statement";
    * variables: var name = value; 
    * if
    * while
    * for
    * fun definitions
    * class definitions
* Closures
* Classes (first-class objects, can be assigned to variables)
* Inheritance (<)
# More:
* OOP can use two approaches: classes (inheritance of classes) and prototypes (inheritance of objects / delegation)
* http://wiki.c2.com/?WaterbedTheory

# Gloassary
* An **argument** is an actual value you pass to a function when you call it. So a function call has an argument list. Sometimes you hear actual parameter used for these.
* A **parameter** is a variable that holds the value of the argument inside the body of the function. Thus, a function declaration has a parameter list. Others call these formal parameters or simply formals.
