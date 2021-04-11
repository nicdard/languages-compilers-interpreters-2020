let (@) f g x = f (g x)

let interpret lexbuf =
  try
    let exp = Parser.main (Lexer.get_parser_token @ Lexer.scanner) lexbuf in
    let _ = Fun.type_check exp in
    let res = Fun.eval exp [] in
    Printf.printf "%s\n%!" (Fun.string_of_value res);
  with 
  | Lexer.Error s -> Printf.eprintf "%s\n%!" s
  | Parser.Error -> Printf.eprintf "Syntax error.\n%!"
  | Fun.Type_error s -> Printf.eprintf "Type error: %s\n%!" s

let () =
  let cin = 
      if Array.length Sys.argv > 1 then
          open_in Sys.argv.(1)
      else
          stdin
  in
  let lexbuf = Lexing.from_channel cin in
  interpret lexbuf