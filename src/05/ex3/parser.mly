%{
open Assembly
let current = ref(1) (* Register 0 is readonly and contains 0 *)

let load i = 
    let r = make_register current in
    (r, IUnOp(Load, r, i))

let bin_op (op: binary_op) (r1, il1) (r2, il2) =
    let result = make_register current in
    (result, IBinOp(op, result, r1, r2)::(il2 @ il1))   
%}

%token <int> INT

%token PLUS
%token MINUS
%token TIMES
%token DIV

%token LEFT_PAR
%token RIGHT_PAR

%token EOL

%left MINUS PLUS
%left TIMES DIV
%nonassoc UMINUS

%start <instruction list>main
%%

main:
    | e = expr EOL 
        (* The first register contains the value 0 *)
        { let (_, il) = e in il }

expr:
    | i = INT
        { let (r, il) = load i in (r, [il]) }
    | LEFT_PAR e = expr RIGHT_PAR
        { e }
    | e1 = expr TIMES e2 = expr
        { bin_op Mul e1 e2 }
    | e1 = expr DIV e2 = expr
        { bin_op Div e1 e2 }
    | e1 = expr PLUS e2 = expr
        { bin_op Add e1 e2 }
    | e1 = expr MINUS e2 = expr
        { bin_op Sub e1 e2 }
    | MINUS e = expr %prec UMINUS
        (* The first register contains the value 0 *)
        { bin_op Sub (R 0, []) e }