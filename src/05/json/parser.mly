%{
    open Json
%}

%token <int> INT
%token <float> FLOAT
%token <string> STRING
%token TRUE
%token FALSE
%token NULL
%token LPAREN RPAREN
%token LCURLY RCURLY
%token COMMA
%token COLON
%token EOF

%start<Json.value option> json_unit

%%

json_unit:
    | EOF       { None }
    | v = value { Some v }

value:
    | i = INT       { Int(i) }
    | f = FLOAT     { Float(f) }
    | s = STRING    { String(s) }
    | TRUE          { Bool(true) }
    | FALSE         { Bool(false) }
    | NULL          { Null }
    | LCURLY fs = separated_list(COMMA, objfield) RCURLY
        { Obj(fs) }
    | LPAREN ls = separated_list(COMMA, value) RPAREN
        { List(ls) }

objfield:
    | k = STRING COLON v = value
        { (k, v) }
