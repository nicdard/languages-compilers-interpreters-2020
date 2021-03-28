{ }

rule token = parse
    | "(*"                  { print_endline "comments 0 start"; comments 0 lexbuf }
    | [ ' ' '\t' '\n' ]   { token lexbuf }
    | [ 'a'-'z' ]+ as word  { Printf.printf "word: %s\n" word; token lexbuf }
    | _ as c                { Printf.printf "char %c\n" c; token lexbuf }
    | eof                   { raise End_of_file }

and comments nesting = parse
    | "*)"          { 
                        Printf.printf "comments (%d) end\n" nesting;
                        if nesting = 0 then token lexbuf
                        else comments (nesting - 1) lexbuf
                    }
    | "(*"          {
                        Printf.printf "comments (%d) start\n" (nesting + 1);
                        comments (nesting + 1) lexbuf
                    }
    | _             { comments nesting lexbuf }
    | eof           { print_endline "comments are not closed"; raise End_of_file }

{
let () =
    let lexbuf = Lexing.from_channel stdin in
    try
        while true do
            token lexbuf
        done
    with End_of_file -> ()
}
