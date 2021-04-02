%{
open Syntax
let (|@|) node loc = { value = node; loc = loc }
%}

%token <int> INT
%token PLUS MINUS TIMES DIV
%token LEFT_PAR RIGHT_PAR
%token EOL

%left PLUS MINUS
%left TIMES DIV
%nonassoc UMINUS

%start <Syntax.expr> main
%%

main:
    | e = expr EOL
        { e }

expr:
    | e = INT
        { (ELiteral e) |@| $loc }
    | LEFT_PAR e = expr RIGHT_PAR
        { e }
    | e1 = expr PLUS e2 = expr
        { (EBinOp(e1, OpPlus, e2)) |@| $loc }
    | e1 = expr MINUS e2 = expr
        { (EBinOp(e1, OpMinus, e2)) |@| $loc }
    | e1 = expr TIMES e2 = expr
        { (EBinOp(e1, OpTimes, e2)) |@| $loc }
    | e1 = expr DIV e2 = expr
        { (EBinOp(e1, OpDiv, e2)) |@| $loc }
    | MINUS e = expr %prec UMINUS
        { (EUnOp(OpNeg, e)) |@| $loc }
    