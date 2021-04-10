{
open Parser

type token = { token_t: Parser.token; position: Lexing.position }

let make_token token_t lexbuf = { token_t = token_t; position = Lexing.lexeme_start_p lexbuf }

let get_parser_token tok = tok.token_t

let create_hashtable size init =
    let tbl = Hashtbl.create size in
    List.iter (fun (key, data) -> Hashtbl.add tbl key data) init;
    tbl

let keyword_table =
    create_hashtable 12 [
        ("NULL", NULL);
        ("true", TRUE);
        ("false", FALSE);
        ("int", INT);
        ("char", CHAR);
        ("void", VOID);
        ("bool", BOOL);
        ("if", IF);
        ("else", ELSE);
        ("for", FOR);
        ("while", WHILE);
        ("return", RETURN);
    ]

let string_of_position p =
    let line_number = p.Lexing.pos_lnum in
    let column = p.Lexing.pos_cnum - p.Lexing.pos_bol + 1 in
    Printf.sprintf "(%d, %d)" line_number column

let incr_linenum lexbuf =
    let pos = lexbuf.Lexing.lex_curr_p 
    in lexbuf.Lexing.lex_curr_p <- { pos with
        Lexing.pos_lnum = pos.Lexing.pos_lnum + 1;
        Lexing.pos_bol = pos.Lexing.pos_cnum;
    }
}

let single_quote = '\''
let double_quote = '"'
let digit = [ '0'-'9' ]
let number = digit+
let alpha = [ 'a'-'z' 'A'-'Z' '_' ]

rule token = parse
    | alpha (alpha | digit)* as word {
        try 
            make_token (Hashtbl.find keyword_table word) lexbuf
        with Not_found ->
            make_token (LID word) lexbuf
    }
    | single_quote ([^ '\''] as c) single_quote {
        make_token (LCHAR c) lexbuf
    }
    | number as n {
        make_token (LINT (int_of_string n)) lexbuf
    }
    | [ ' ' '\t' ] {
        (* remove whitespaces *)
        token lexbuf
    }
    | '\n' {
        incr_linenum lexbuf; token lexbuf 
    }
    | "&&" {
        make_token AND lexbuf
    }
    | "||" {
        make_token OR lexbuf
    }
    | "!=" {
        make_token NEQ lexbuf
    }
    | '!' {
        make_token NOT lexbuf
    }
    | '+' {
        make_token PLUS lexbuf 
    }
    | '-' {
        make_token MINUS lexbuf 
    }
    | '*' {
        make_token STAR lexbuf 
    }
    | '/' {
        make_token DIV lexbuf 
    }
    | '%' {
        make_token MOD lexbuf 
    }
    | '<' {
        make_token LS lexbuf 
    }
    | "<=" {
        make_token LSEQ lexbuf 
    }
    | "==" {
        make_token EQ lexbuf 
    }
    | ">=" {
        make_token GTEQ lexbuf 
    }
    | '>' {
        make_token GT lexbuf 
    }
    | '(' {
        make_token LPAR lexbuf 
    }
    | ')' {
        make_token RPAR lexbuf 
    }
    | '[' {
        make_token LBRACKET lexbuf 
    }
    | ']' {
        make_token RBRACKET lexbuf 
    }
    | '{' {
        make_token LBRACE lexbuf 
    }
    | '}' {
        make_token RBRACE lexbuf 
    }
    | ';' {
        make_token SEMICOLON lexbuf
    }
    | ',' {
        make_token COMMA lexbuf 
    }
    | '&' {
        make_token ADDRESS lexbuf 
    }
    | '=' {
        make_token ASSIGN lexbuf
    }
    | "//" {
        single_line_comment lexbuf
    }
    | "/*" {
        multi_line_commment lexbuf
    }
    | _ as c {
        Util.raise_lexer_error lexbuf (Printf.sprintf "Illegal character %s" (Char.escaped c)) 
    }
    | eof {
        make_token EOF lexbuf
    }

and single_line_comment = parse
    | '\n' {
        incr_linenum lexbuf; token lexbuf
    }
    | _ {
        (* Remove the comments *)
        single_line_comment lexbuf
    }
    | eof {
        make_token EOF lexbuf
    }

and multi_line_commment = parse
    | "*/" {
        token lexbuf
    }
    | '\n' {
        incr_linenum lexbuf; multi_line_commment lexbuf
    } 
    | _ {
        multi_line_commment lexbuf
    }
    | eof {
        make_token EOF lexbuf
    }
