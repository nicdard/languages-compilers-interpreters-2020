let algebraic = ref true

let () =
  Arg.parse [
    "--algebraic", Arg.Set algebraic, " Use algebraic (that is infix) notation";
    "--reverse", Arg.Clear algebraic, " Use reverse (that is polish) notation";
  ] ignore (Printf.sprintf "Usage %s <options>" Sys.argv.(0))

let main = 
  if !algebraic then
    Algebraic.main
  else
    Reverse.main
    
let process (line: string) =
  let linebuf = Lexing.from_string line in
  try
    Printf.printf "%d" (main Lexer.token linebuf)
  with
  | Lexer.Error msg -> Printf.eprintf "%s%!" msg
  | Algebraic.Error 
  | Reverse.Error -> Printf.eprintf "Syntax error at %d.\n" (Lexing.lexeme_start linebuf) 

let process (line: string option) =
  match line with
  | None -> ()
  | Some l -> process l

let rec repeat channel =
  let optional_line, continue = Lexer.line channel in
  process optional_line;
  if continue then
    repeat channel

let () =
    repeat (Lexing.from_channel stdin)