open Lexing

module I =
  Parser.MenhirInterpreter

let rec loop lexbuf (checkpoint: int I.checkpoint) =
  match checkpoint with
  | I.InputNeeded _env ->
    let token = Lexer.token lexbuf in
    let startp = lexbuf.lex_start_p in
    let endp = lexbuf.lex_start_p in
    let checkpoint = I.offer checkpoint (token, startp, endp) in
    loop lexbuf checkpoint
  | I.Shifting _
  | I.AboutToReduce _ ->
    let checkpoint = I.resume checkpoint in
    loop lexbuf checkpoint
  | I.HandlingError _env ->
    Printf.eprintf "At offset %d: syntax error.\n%!" (lexeme_start lexbuf)
  | I.Accepted v ->
    Printf.printf "%d\n%!" v
  | I.Rejected -> assert false

let _ = loop (* silence OCaml's unuse-value warning about loop *)

(* A more succint version of the previous loop using Menhir 
   functions [lexer_lexbuf_to_supplier] and [loop_handle]. *)
let succeed (v: int) =
  Printf.printf "%d\n%!" v

let fail lexbuf (_: int I.checkpoint) =
  Printf.eprintf "At offset %d: syntax error.\n%!" (lexeme_start lexbuf)

let loop lexbuf result =
  let supplier = I.lexer_lexbuf_to_supplier Lexer.token lexbuf in
  I.loop_handle succeed (fail lexbuf) supplier result


let process (line: string) =
  let lexbuf = from_string line in
  try
    loop lexbuf (Parser.Incremental.main lexbuf.lex_curr_p)
  with
  | Lexer.Error msg ->
    Printf.eprintf "%s%!" msg

let process (optional_line : string option) =
  match optional_line with
  | None ->
      ()
  | Some line ->
      process line

let rec repeat channel =
  (* Attempt to read one line. *)
  let optional_line, continue = Lexer.line channel in
  process optional_line;
  if continue then
    repeat channel

let () =
  repeat (from_channel stdin)

