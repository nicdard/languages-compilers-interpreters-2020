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

%start <int>main
%%

main:
    | e = expr EOL  
        { e }

expr:
    | i = INT
        { i }
    | LEFT_PAR e = expr RIGHT_PAR
        { e }
    | e1 = expr TIMES e2 = expr
        { e1 * e2 }
    | e1 = expr DIV e2 = expr
        { e1 / e2 }
    | e1 = expr PLUS e2 = expr
        { e1 + e2 }
    | e1 = expr MINUS e2 = expr
        { e1 - e2 }
    | MINUS e = expr %prec UMINUS
        { - e }