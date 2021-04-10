# Context Free Grammar for MicroC

## Expressions

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