%{
  open Fun
%}

%token IF THEN ELSE LET IN FUN TRUE FALSE TINT TBOOL
%token PLUS MINUS TIMES DIV
%token EQ LESS GREATER LEQ GEQ COLON ARROW
%token LPAREN RPAREN EOF
%token <string>ID
%token <int>LINT

%nonassoc prec_let
%left ARROW
%nonassoc EQ LESS GREATER LEQ GEQ
%left PLUS MINUS
%left TIMES DIV



%start <expr> main

%%

main: e = expr EOF { e }
;

expr:
  | e = simple_expr
    { e }
  | e1 = expr PLUS e2 = expr
    { Prim("+", e1, e2) }
  | e1 = expr MINUS e2 = expr
    { Prim("-", e1, e2) }
  | e1 = expr TIMES e2 = expr
    { Prim("*", e1, e2) }
  | e1 = expr DIV e2 = expr
    { Prim("/", e1, e2) }
  | e1 = expr LESS e2 = expr
    { Prim("<", e1, e2) }
  | e1 = expr GREATER e2 = expr
    { Prim(">", e1, e2) }
  | e1 = expr LEQ e2 = expr
    { Prim("<=", e1, e2) }
  | e1 = expr GEQ e2 = expr
    { Prim(">=", e1, e2) }
  | e1 = expr EQ e2 = expr
    { Prim("=", e1, e2) }
  | IF cond=expr THEN te=expr ELSE ee=expr  %prec prec_let
    { If(cond, te, ee) }
  | LET id=ID EQ e1=expr IN e2=expr   %prec prec_let
    { Let(id, e1, e2) }
  | FUN f=ID LPAREN x=ID COLON t1=ptype RPAREN COLON t2=ptype  EQ body = expr IN e2 = expr %prec prec_let
    { Letfun(f, x, Tfun(t1, t2), body, e2) }
  | e = application
    { e }
;

simple_expr:
  | i = LINT
    { CstI(i) }
  | TRUE
    { CstB(true) }
  | FALSE
    { CstB(false) }
  | id = ID
    { Var(id) }
  | LPAREN e = expr RPAREN
    { e }
;

ptype:
  | TINT
    { Tint }
  | TBOOL
    { Tbool }
  | LPAREN t=ptype RPAREN
    { t }
  | t1=ptype ARROW t2=ptype
    { Tfun(t1, t2) }
;

application:
  | e1 = simple_expr e2=simple_expr
    { Call(e1, e2) }
  | e1 = application e2 = simple_expr
    { Call(e1, e2) }
;
