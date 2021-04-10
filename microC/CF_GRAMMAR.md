# Context Free Grammar for MicroC

```
program ::=
    | topdecl* EOF

topdecl ::=
    | vardecl SEMICOLON
    | fundecl

fundecl ::=
    | type LID LPAR ((vardecl COLON)* vardecl)? RPAR block

block ::=
    | LBRACE (stmt | vardecl SEMICOLON)* RBRACE

stmt ::=
    | expression SEMICOLON
    | RETURN expression SEMICOLON
    | block
    | WHILE LPAR expression RPAR stmt
    | FOR LPAR expression? SEMICOLON expression? SEMICOLON expression RPAR stmt
    | IF LPAR expression RPAR stmt ELSE stmt
    | IF LPAR expression RPAR stmt

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
    | lexpression
    | repression

rexpression ::=
    | literal
    | unary
    | binary
    | grouping
    | assign
    | address
    | call

lexpression ::=
    | LID
    | LPAR lexpression RPAR
    | STAR lexpression
    | STAR NULL
    | STAR address
    | lexpression LBRACKET expression RBRACKET
    
address ::=
    | ADDRESS lexpression

assign ::=
    | lexpression ASSIGN expression // an lvalue can appear either to the left or to the right, a rvalue only to the right

call ::=
    | LID LPAR ((expression COMMA)* expression)?  RPAR

literal ::=
    | LINT
    | LCHAR
    | LBOOL // "true" "false"
    | NULL // "NULL"

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