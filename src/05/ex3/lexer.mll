{
open Parser

exception Error of string
}

let character = [ 'a'-'z' ]
let digit = [ '0'-'9' ]
let number = digit+

(* This rule looks for a single line, terminated with '\n' or eof.
   It returns a pair of an optional string (the line that was found)
   and a Boolean flag (false if eof was reached). *)
rule line = parse
    | ([^ '\n']* '\n') as line      { (Some line, true) }
    | eof                           { (None, false) }
    | ([^ '\n']+ eof) as line       { (Some (line ^ "\n"), false) }

(* This rule analyzes a single line and turns it into a stream of
   tokens. *)

and token = parse
    | [' ' '\t']                    { token lexbuf }
    | '\n'                          { EOL }
    | number as n                   { INT (int_of_string n)}
    | '+'                           { PLUS }
    | '-'                           { MINUS }
    | '*'                           { TIMES }
    | '/'                           { DIV }
    | '('                           { LEFT_PAR }
    | ')'                           { RIGHT_PAR }
    | _ as c                        { 
        let error = Printf.sprintf "Unexpected character '%c' at offset %d" c (Lexing.lexeme_start lexbuf) in
        raise (Error error)
    }