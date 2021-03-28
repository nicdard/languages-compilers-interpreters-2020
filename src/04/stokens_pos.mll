{
type token_type = 
    | EOF 
    | ID of string 
    | IF 
    | NUM of int 
    | PLUS
and token = { pos: Lexing.position; tok_type: token_type } 

let string_of_token = function
    | EOF       -> "EOF"
    | ID(s)     -> Printf.sprintf "ID(%s)" s
    | IF        -> "IF"
    | NUM(i)    -> Printf.sprintf "NUM(%d)" i
    | PLUS      -> "PLUS"

let string_of_position p =
    let line_number = p.Lexing.pos_lnum in
    let column = p.Lexing.pos_cnum - p.Lexing.pos_bol + 1 in
    Printf.sprintf "(%d, %d)" line_number column

let rec iterate scanner =
    match scanner () with
    | { tok_type = EOF; pos = _ } -> ()
    | tok -> Printf.printf "%s @ %s\n" (string_of_token tok.tok_type) (string_of_position tok.pos); iterate scanner

let make_token ttype lexbuf = { tok_type = ttype; pos = Lexing.lexeme_start_p lexbuf }
}

let digit = [ '0'-'9' ]
let letter = [ 'a'-'z' 'A'-'Z' ]
let identifier = letter ( letter | digit | '_' )*
let number = digit+

rule token = parse
| [ ' ' '\n' '\t' ]         { token lexbuf }
| '+'                       { make_token PLUS lexbuf }
| "if"                      { make_token IF lexbuf }
| identifier as id          { make_token (ID(id)) lexbuf }
| number as num             { make_token (NUM(int_of_string num)) lexbuf }
| eof                       { make_token EOF lexbuf }

{
let () =
    let lexbuf = Lexing.from_channel stdin in
    iterate (fun () -> token lexbuf)
}
