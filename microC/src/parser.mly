/*
* MicroC Parser specification
*/

%{
open Ast
let id = ref(0)
let make_annotated_node node loc = incr id; { loc = loc; node = node; id = !id }
%}

/* Tokens declarations */

%token <char> LCHAR
%token <int> LINT
%token <string> LID
%token NULL
%token TRUE FALSE

%token AND OR NOT
%token GT GTEQ EQ LSEQ LS NEQ
%token PLUS MINUS STAR DIV MOD

%token LPAR RPAR
%token LBRACKET RBRACKET
%token LBRACE RBRACE

%token SEMICOLON COMMA ADDRESS ASSIGN

%token INT CHAR VOID BOOL
%token IF ELSE FOR WHILE RETURN

%token EOF

/* Precedence and associativity specification */

/* Disambiguate if then else stmts */
%nonassoc NOELSE
%nonassoc ELSE

%right ASSIGN
%left OR
%left AND
%left NEQ EQ
%nonassoc GT GTEQ LSEQ LS
%left PLUS MINUS
%left STAR DIV MOD
%nonassoc NEG NOT ADDRESS
%nonassoc LBRACKET


/* Starting symbol */

// %start program
// %type <Ast.program> program    /* the parser returns a Ast.program value */
%start program
%type <Ast.program> program

%%

program:
  | declarations = list(topdecl) EOF
    { Prog(declarations) }
;

topdecl:
  | v = topvardecl
    { v }
  | f = fundecl
    { f }
;

fundecl:
  | typ = ctype fname = LID LPAR formals = separated_list(COMMA, vardecl) RPAR body = block
    { make_annotated_node (Fundecl { typ; fname; formals; body }) $loc }

block:
  | LBRACE declarations = list(localvar) RBRACE
    { make_annotated_node (Block declarations) $loc }

topvardecl:
  | v = vardecl SEMICOLON
    { let (typ, id) = v in
      make_annotated_node (Vardec (typ, id)) $loc }

localvar:
  | v = vardecl SEMICOLON
    { let (typ, id) = v in
      make_annotated_node (Dec (typ, id)) $loc }

vardecl:
  | t = ctype var = vardesc
    { let (type_constr, v) = var in
      ((type_constr t), v) }
;

vardesc:
  | id = LID
    { ((fun t -> t), id) }
  | STAR var = vardesc
    { let (constr, i) = var in
      let pointer_constructor t = TypP (constr t) in
      (pointer_constructor, i) }
  | LPAR var = vardesc RPAR
    { var }
  | var = vardesc LBRACKET RBRACKET
    { let (constr, i) = var in
      let array_constructor t = TypA (constr t, None) in
      (array_constructor, i) }
  | var = vardesc LBRACKET n = LINT RBRACKET
    { let (constr, i) = var in
      let array_constructor t = TypA (constr t, Some n) in
      (array_constructor, i) }
;

ctype:
  | INT
    { TypI }
  | BOOL
    { TypB }
  | CHAR
    { TypC }
  | VOID
    { TypV }
;

expression:
  | l = literal
    { l }
  | u = unary
    { u }
  | b = binary
    { b }
  | g = grouping
    { g }
;

grouping:
  | LPAR e = expression RPAR
    { e }
;

binary:
  | e1 = expression PLUS e2 = expression
    { make_annotated_node (BinaryOp (Add, e1, e2)) $loc }
  | e1 = expression STAR e2 = expression
    { make_annotated_node (BinaryOp (Mult, e1, e2)) $loc }
  | e1 = expression MINUS e2 = expression
    { make_annotated_node (BinaryOp (Sub, e1, e2)) $loc }
  | e1 = expression DIV e2 = expression
    { make_annotated_node (BinaryOp (Div, e1, e2)) $loc }
  | e1 = expression MOD e2 = expression
    { make_annotated_node (BinaryOp (Mod, e1, e2)) $loc }
  | e1 = expression EQ e2 = expression
    { make_annotated_node (BinaryOp (Equal, e1, e2)) $loc }
  | e1 = expression NEQ e2 = expression
    { make_annotated_node (BinaryOp (Neq, e1, e2)) $loc }
  | e1 = expression LS e2 = expression
    { make_annotated_node (BinaryOp (Less, e1, e2)) $loc }
  | e1 = expression LSEQ e2 = expression
    { make_annotated_node (BinaryOp (Leq, e1, e2)) $loc }
  | e1 = expression GT e2 = expression
    { make_annotated_node (BinaryOp (Greater, e1, e2)) $loc }
  | e1 = expression GTEQ e2 = expression
    { make_annotated_node (BinaryOp (Geq, e1, e2)) $loc }
  | e1 = expression AND e2 = expression
    { make_annotated_node (BinaryOp (And, e1, e2)) $loc }
  | e1 = expression OR e2 = expression
    { make_annotated_node (BinaryOp (Or, e1, e2)) $loc }
;

unary:
  | MINUS e = expression %prec NEG
    { make_annotated_node (UnaryOp (Neg, e)) $loc }
  | NOT e = expression
    { make_annotated_node (UnaryOp (Not, e)) $loc }
;

literal:
  | n = LINT
    { make_annotated_node (ILiteral n) $loc }
  | c = LCHAR
    { make_annotated_node (CLiteral c) $loc }
  | TRUE
    { make_annotated_node (BLiteral true) $loc }
  | FALSE
    { make_annotated_node (BLiteral false) $loc }
  | NULL // Zero is an invalid value for a pointer, here we use the definition from stdlib.h
    { make_annotated_node (ILiteral 0) $loc }
;