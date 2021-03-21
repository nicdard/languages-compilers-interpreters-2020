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