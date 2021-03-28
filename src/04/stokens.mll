{
type token = 
    | EOF 
    | ID of string 
    | IF 
    | NUM of int 
    | PLUS 

let string_of_token = function
    | EOF       -> "EOF"
    | ID(s)     -> Printf.sprintf "ID(%s)" s
    | IF        -> "IF"
    | NUM(i)    -> Printf.sprintf "NUM(%d)" i
    | PLUS      -> "PLUS"

let rec iterate scanner =
    match scanner () with
    | EOF -> ()
    | tok -> Printf.printf "%s\n" (string_of_token tok); iterate scanner
}

let digit = [ '0'-'9' ]
let letter = [ 'a'-'z' 'A'-'Z' ]
let identifier = letter ( letter | digit | '_' )*
let number = digit+

rule token = parse
| [ ' ' '\n' '\t' ]         { token lexbuf }
| '+'                       { PLUS }
| "if"                      { IF }
| identifier as id          { ID(id) }
| number as num             { NUM(int_of_string num) }
| eof                       { EOF }

{
let () =
    let lexbuf = Lexing.from_channel stdin in
    iterate (fun () -> token lexbuf)
}
