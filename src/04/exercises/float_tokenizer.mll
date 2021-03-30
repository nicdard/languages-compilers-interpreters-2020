{
(* Exercise 1 *)
}

let dot = '.'
let digit = [ '0'-'9' ]
let sign = [ '+' '-' ]
let exponent = [ 'e' 'E' ]
let float = '-'? digit+  ( '.' digit* )? ( exponent sign? digit+ )?

let whitespace = ' ' | '\t' | '\n' | '\r' | "\r\n"

rule tokenizer = parse
    | float as n        { Printf.eprintf "%.4f\n" (float_of_string n); tokenizer lexbuf }   
    | whitespace        { tokenizer lexbuf }
    | _ as c            { Printf.eprintf "Unexpected character: %c\n" c; exit 1 }
    | eof               { () }

{
let () =
    let cin = if Array.length Sys.argv > 1
            then open_in Sys.argv.(1)
            else stdin
    in let lexbuf = Lexing.from_channel cin in
    tokenizer lexbuf
}