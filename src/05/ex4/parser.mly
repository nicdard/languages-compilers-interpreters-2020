%{
open Fun
%}


%token <string> ID
%token <int> INT
%token TRUE FALSE

%token MINUS PLUS
%token STAR DIV

%token EQUAL LESS

%token IF THEN ELSE
%token LET IN
%token FUN

%token LPAR RPAR
%token EOF

%nonassoc prec_let
%nonassoc EQUAL LESS
%left PLUS MINUS
%left STAR DIV

%start <expr>main

%%

main: e = expr EOF { e }
;

expr:
    | e = simple_expr
        { e }
    | LET id = ID EQUAL v = expr IN scope = expr %prec prec_let
        { Let(id, v, scope) }
    | IF b = expr THEN e1 = expr ELSE e2 = expr %prec prec_let
        { If(b, e1, e2) }
    | FUN f = ID x = ID EQUAL body = expr IN scope = expr %prec prec_let
        { Letfun(f, x, body, scope) }
    | e1 = expr PLUS e2 = expr
        { Prim("+", e1, e2) }
    | e1 = expr MINUS e2 = expr
        { Prim("-", e1, e2) }
    | e1 = expr STAR e2 = expr
        { Prim("*", e1, e2) }
    | e1 = expr DIV e2 = expr
        { Prim("/", e1, e2) }
    | e1 = expr EQUAL e2 = expr
        { Prim("=", e1, e2) }
    | e1 = expr LESS e2 = expr
        { Prim("<", e1, e2) }
    | e = application
        { e }
;

simple_expr:
    | i = INT
        { CstI(i) }
    | TRUE
        { CstB(true) }
    | FALSE
        { CstB(false) }
    | id = ID
        { Var(id) }
    | LPAR e = expr RPAR
        { e }
;

application:
    | f = simple_expr v = simple_expr
        { Call(f, v) }
    | f = application v = simple_expr
        { Call(f, v) }