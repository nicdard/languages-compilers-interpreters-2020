let process (line: string) =
  let linebuf = Lexing.from_string line in
  try
    List.iter (fun el -> Printf.printf "%s\n%!" (Assembly.string_of_instruction el)) (Parser.main Lexer.token linebuf)
  with
  | Lexer.Error msg ->
      Printf.eprintf "%s%!" msg
  | Parser.Error ->
      Printf.eprintf "Syntax error at offset %d.\n" (Lexing.lexeme_start linebuf) 
      
let process (optional_line: string option) =
  match optional_line with
  | None -> ()
  | Some line -> process line

let rec repeat channel =
  let optional_line, continue = Lexer.line channel in
  process optional_line;
  if continue then
    repeat channel

let () =
  repeat (Lexing.from_channel stdin)