%token <float> FLOAT

%token EXP
%token LOG
%token SIN
%token COS
%token PLUS
%token MINUS
%token TIMES
%token DIV

%token LEFT_PAR
%token RIGHT_PAR

%token EOL

%left MINUS PLUS
%left TIMES DIV
%left EXP
%nonassoc UMINUS

%start <float>main
%%

main:
    | e = expr EOL  
        { e }

expr:
    | f = FLOAT
        { f }
    | LEFT_PAR e = expr RIGHT_PAR
        { e }
    | LOG LEFT_PAR e = expr RIGHT_PAR
        { log e }
    | SIN LEFT_PAR e = expr RIGHT_PAR
        { sin e }
    | COS LEFT_PAR e = expr RIGHT_PAR
        { cos e }
    | e1 = expr EXP e2 = expr
        { e1 ** e2 }
    | e1 = expr TIMES e2 = expr
        { e1 *. e2 }
    | e1 = expr DIV e2 = expr
        { e1 /. e2 }
    | e1 = expr PLUS e2 = expr
        { e1 +. e2 }
    | e1 = expr MINUS e2 = expr
        { e1 -. e2 }
    | MINUS e = expr %prec UMINUS
        { -. e }