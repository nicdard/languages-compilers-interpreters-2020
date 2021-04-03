{
open Parser

exception Error of string

let create_hashtable size init =
    let tbl = Hashtbl.create size in
    List.iter (fun (key, data) -> Hashtbl.add tbl key data) init;
    tbl

let keyword_table =
    create_hashtable 3 [
        ("log", LOG);
        ("sin", SIN);
        ("cos", COS);
    ]
}

let character = [ 'a'-'z' ]
let digit = [ '0'-'9' ]
let number = digit+
let float = digit+ '.' digit*

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
    | character+ as word            { 
        try
            Hashtbl.find keyword_table word
        with Not_found -> 
            let error = Printf.sprintf "Unexpected word '%s' at offset %d" word (Lexing.lexeme_start lexbuf) in
            raise (Error error)
    }
    | number
    | float as f                    { FLOAT(float_of_string f) }
    | '+'                           { PLUS }
    | '-'                           { MINUS }
    | "**"                          { EXP }
    | '*'                           { TIMES }
    | '/'                           { DIV }
    | '('                           { LEFT_PAR }
    | ')'                           { RIGHT_PAR }
    | _ as c                        { 
        let error = Printf.sprintf "Unexpected character '%c' at offset %d" c (Lexing.lexeme_start lexbuf) in
        raise (Error error)
    }