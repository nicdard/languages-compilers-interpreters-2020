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
  | LBRACE declarations = list(stmtordec) RBRACE
    { make_annotated_node (Block declarations) $loc }
;

stmtordec:
  | s = stmt
    { make_annotated_node (Stmt s) $loc }
  | dec = localvar
    { dec }
;

stmt:
  | e = expression SEMICOLON
    { make_annotated_node (Expr e) $loc }
  | b = block
    { b }
  | RETURN e = option(expression) SEMICOLON
    { make_annotated_node (Return e) $loc }
  | IF LPAR guard = expression RPAR t = stmt %prec NOELSE
    { let empty_else_block = make_annotated_node (Block []) $loc in
      make_annotated_node (If (guard, t, empty_else_block)) $loc }
  | IF LPAR guard = expression RPAR t = stmt ELSE t2 = stmt
    { make_annotated_node (If (guard, t, t2)) $loc }
  | WHILE LPAR guard = expression RPAR body = block
    { make_annotated_node (While (guard, body)) $loc }
  | FOR LPAR init = option(expression) SEMICOLON g = option(expression) SEMICOLON incr = option(expression) RPAR body = stmt
    { let true_lit = make_annotated_node (BLiteral true) $loc in
      let true_expr = make_annotated_node (Expr true_lit) $loc in
      let init_expr = make_annotated_node (Expr (Option.value init ~default:true_lit)) $loc in
      let init_stmt = make_annotated_node (Stmt init_expr) $loc in
      let incr_stmt = make_annotated_node (Stmt true_expr) $loc in
      let body_stmt = make_annotated_node (Stmt body) $loc in
      let for_body = make_annotated_node (Block [body_stmt; incr_stmt]) $loc in
      let guard = Option.value g ~default:true_lit in
      let loop = make_annotated_node (While (guard, for_body)) $loc in
      let loop_stmt = make_annotated_node (Stmt loop) $loc in
      make_annotated_node (Block [init_stmt; loop_stmt]) $loc }
;

topvardecl:
  | v = vardecl SEMICOLON
    { let (typ, id) = v in
      make_annotated_node (Vardec (typ, id)) $loc }
;

localvar:
  | v = vardecl SEMICOLON
    { let (typ, id) = v in
      make_annotated_node (Dec (typ, id)) $loc }
;

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
  | e = lexpression 
    { make_annotated_node (Access e) $loc }
  | e = rexpression
    { e }
;

rexpression:
  | l = literal
    { l }
  | u = unary
    { u }
  | b = binary
    { b }
  | g = grouping
    { g }
  | a = assign
    { a }
  | a = address
    { a }
  | c = call
    { c }
;

lexpression:
  | id = LID
    { make_annotated_node (AccVar id) $loc }
  | LPAR lvalue = lexpression RPAR
    { lvalue }
  | STAR lvalue = lexpression
    { let access = make_annotated_node (Access lvalue) $loc in
      make_annotated_node (AccDeref access) $loc }
  | STAR n = nullptr
    { make_annotated_node (AccDeref n) $loc }
  | STAR a = address
    { make_annotated_node (AccDeref a) $loc }
  | arr = lexpression LBRACKET index = expression RBRACKET
    { make_annotated_node (AccIndex (arr, index)) $loc }
;

address:
  | ADDRESS lvalue = lexpression
    { make_annotated_node (Addr lvalue) $loc }
;

assign:
  | lhs = lexpression ASSIGN rvalue = expression
    { make_annotated_node (Assign (lhs, rvalue)) $loc }
;

grouping:
  | LPAR e = rexpression RPAR
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
  | n = nullptr
    { n }
;

nullptr:
  | NULL // Zero is an invalid value for a pointer, here we use the definition from stdlib.h
    { make_annotated_node (ILiteral 0) $loc }
;

call:
  | fname = LID LPAR args = separated_list(COMMA, expression) RPAR
    { make_annotated_node (Call (fname, args)) $loc }
