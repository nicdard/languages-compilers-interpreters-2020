{
(* Exercise 2 *)
}

rule c_strings = parse
    | "\\n"             { Printf.printf "\n"; c_strings lexbuf }
    | "\\t"             { Printf.printf "\t"; c_strings lexbuf }
    | "\\'"             { Printf.printf "'"; c_strings lexbuf }
    | "\\\""            { Printf.printf "\""; c_strings lexbuf }
    | "\\\\"            { Printf.printf "\\"; c_strings lexbuf }
    | _ as c            { Printf.printf "%c" c; c_strings lexbuf }
    | eof               { () }

{
let () =
    let cin = if Array.length Sys.argv > 1
        then open_in Sys.argv.(1)
        else stdin
    in let lexbuf = Lexing.from_channel cin
    in c_strings lexbuf
}
