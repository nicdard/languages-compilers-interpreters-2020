# Context Free Grammar for MicroC

```
program ::=
    | topdecl* EOF

topdecl ::=
    | vardecl SEMICOLON

vardecl ::=
    | type vardesc

vardesc ::= 
    | LID
    | STAR vardesc
    | LPAR vardesc RPAR
    | vardesc LBRACKET RBRACKET // should not be possible in top declarations 
    | vardesc LBRACKET LINT RBRACKET

type ::=
    | INT
    | CHAR
    | BOOL
    | VOID

expression ::=
    | literal
    | unary
    | binary
    | grouping

literal ::=
    | LINT
    | LCHAR
    | LBOOL (* "true" "false" *)
    | NULL ( "NULL" )

grouping ::= 
    | LPAR expression RPAR

unary ::=
    | MINUS expression
    | NOT expression

binary ::=
    | expression operator expression

operator ::=
    | GT 
    | GTEQ 
    | EQ 
    | LSEQ 
    | LS 
    | NEQ
    | AND
    | OR
    | PLUS
    | MINUS
    | STAR
    | DIV
    | MOD 
```