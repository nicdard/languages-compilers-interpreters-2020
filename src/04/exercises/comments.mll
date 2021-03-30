{
(* Exercise 3 *)
let ncomm = ref 0
let ncode = ref 0
exception Syntax_error of string
}

let comment_start = "(*"
let comment_end = "*)"

rule code = parse
    | comment_start { comments 0 lexbuf }
    | _             { incr ncode; code lexbuf }
    | comment_end   { raise (Syntax_error "Unexpected '*)'") }
    | eof           { () }

(* Track the depth of comments *)
and comments depth = parse
    | comment_end   {
                        if depth = 0 
                            then code lexbuf
                            else comments (depth - 1) lexbuf  
                    }
    | comment_start { comments (depth + 1) lexbuf }
    | _             { incr ncomm; comments depth lexbuf }
    | eof           { () }

{
let () =
    let cin = if Array.length Sys.argv > 1
        then open_in Sys.argv.(1)
        else stdin
    in let lexbuf = Lexing.from_channel cin
    in code lexbuf; Printf.printf 
        "# chars in comments: %d\n\
         # chars in code: %d\n\
         # comment-density %f\n" 
        !ncomm 
        !ncode 
        ((float_of_int !ncomm) /. (float_of_int !ncode))
}
