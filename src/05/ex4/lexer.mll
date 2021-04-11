{
(* 
Exercise 5: A scanner for the FUN language 

This simple version just parses all the tokens and
print them to the standard output toghether with
their position in the source code.
*)
open Parser

exception Error of string

type token = { token_t: Parser.token; position: Lexing.position }

let make_token token_t lexbuf = { token_t = token_t; position = Lexing.lexeme_start_p lexbuf }

let get_parser_token tok = tok.token_t

let string_of_token = function
    | DIV               -> "DIV"
    | ELSE              -> "ELSE"
    | EOF               -> "EOF"
    | EQUAL             -> "EQUAL"
    | FALSE             -> "FALSE"
    | FUN               -> "FUN"
    | LESS              -> "LESS"
    | LET               -> "LET"
    | LPAR              -> "("
    | ID(id)            -> Printf.sprintf "ID(%s)" id
    | IF                -> "IF"
    | IN                -> "IN"
    | INT(i)            -> Printf.sprintf "INT(%d)" i        
    | MINUS             -> "MINUS"
    | PLUS              -> "PLUS"
    | RPAR              -> ")"
    | STAR              -> "STAR"
    | THEN              -> "THEN"
    | TRUE              -> "TRUE"

let string_of_position p =
    let line_number = p.Lexing.pos_lnum in
    let column = p.Lexing.pos_cnum - p.Lexing.pos_bol + 1 in
    Printf.sprintf "(%d, %d)" line_number column

let create_hashtable size init =
    let tbl = Hashtbl.create size in
    List.iter (fun (key, data) -> Hashtbl.add tbl key data) init;
    tbl

let keyword_table =
    create_hashtable 8 [
        ("else", ELSE);
        ("false", FALSE);
        ("fun", FUN);
        ("let", LET);
        ("if", IF);
        ("in", IN);
        ("then", THEN);
        ("true", TRUE);
    ]

let incr_linenum lexbuf =
    let pos = lexbuf.Lexing.lex_curr_p 
    in lexbuf.Lexing.lex_curr_p <- { pos with
        Lexing.pos_lnum = pos.Lexing.pos_lnum + 1;
        Lexing.pos_bol = pos.Lexing.pos_cnum;
    }
}

let digit = [ '0'-'9' ]
let number = digit+
let letter = [ 'a'-'z' 'A'-'Z' ]
let id = letter ( digit | letter )* 

rule scanner = parse
    | number as n           { make_token (INT(int_of_string n)) lexbuf }
    | id as word            {
                                try 
                                    make_token (Hashtbl.find keyword_table word) lexbuf
                                with Not_found ->
                                    make_token (ID word) lexbuf
                            }
    | '+'                   { make_token PLUS lexbuf }
    | '-'                   { make_token MINUS lexbuf }
    | '*'                   { make_token STAR lexbuf }
    | '/'                   { make_token DIV lexbuf }
    | '='                   { make_token EQUAL lexbuf }
    | '<'                   { make_token MINUS lexbuf }
    | '('                   { make_token LPAR lexbuf }
    | ')'                   { make_token RPAR lexbuf }
    | [ ' ' '\t' ]          (* remove whitespaces *)
                            { scanner lexbuf }
    | '\n'                  { incr_linenum lexbuf; scanner lexbuf }      
    | eof                   { make_token EOF lexbuf }
    | _ as c                { 
        let position = Lexing.lexeme_start_p lexbuf in
        let error = Printf.sprintf "Unrecognised character '%c' @ %s\n" c (string_of_position position) in
        raise (Error error) 
    }              
