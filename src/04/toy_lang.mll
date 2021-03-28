{
open Printf

let create_hashtable size init =
    let tbl = Hashtbl.create size in
    List.iter (fun (key, data) -> Hashtbl.add tbl key data) init;
    tbl

type token =
    | IF
    | THEN
    | ELSE
    | BEGIN
    | END
    | FUNCTION
    | ID of string
    | OP of char
    | INT of int
    | FLOAT of float
    | CHAR of char

let keyword_table =
    create_hashtable 6 [
        ("if", IF);
        ("then", THEN);
        ("else", ELSE);
        ("begin", BEGIN);
        ("end", END);
        ("function", FUNCTION)
    ]
}

let digit = ['0'-'9']
let inumber = digit+
let fnumber = digit+ '.' digit* 
let letter = ['a'-'z' 'A'-'Z']
let id = letter ( digit | letter )*

rule toy_language = parse
    | inumber as i      {
                            let inum = int_of_string i in 
                            printf "%s: INT(%d)\n" i inum;
                            INT inum
                        }
    | fnumber as f      {
                            let fnum = float_of_string f in
                            printf "%s: FLOAT(%f)\n" f fnum;
                            FLOAT fnum
                        }
    | id as word        {
                            try
                                let token = Hashtbl.find keyword_table word in
                                printf "%s: keyword\n" word;
                                token
                            with Not_found ->
                                printf "%s: identifier\n" word;
                                ID word
                        }
    | '+'
    | '*'
    | '-'
    | '/' as op         {
                            printf "%c: operator\n" op; 
                            OP op
                        }
    | '{' [^ '\n']* '}' (* remove one-line comments *)
    | [ ' ' '\t' '\n' ] (* remove whitespaces *)
                        {
                            toy_language lexbuf
                        }
    | _ as c            { printf "Unrecognised character: %c\n" c; CHAR c }
    | eof               { raise End_of_file }


{
let rec parse lexbuf =
    let _ = toy_language lexbuf in
    parse lexbuf

let () =
    let cin = 
        if Array.length Sys.argv > 1 then
            open_in Sys.argv.(1)
        else
            stdin
    in
    let lexbuf = Lexing.from_channel cin in
    try
        parse lexbuf
    with End_of_file -> () 
}