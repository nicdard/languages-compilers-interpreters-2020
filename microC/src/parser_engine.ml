let (@) f g x = f (g x)

let parse lexbuf =
  try
    let tokens_provider = (Scanner.get_parser_token @ Scanner.token) in
    Parser.program tokens_provider lexbuf
  with
  | Parser.Error -> Util.raise_syntax_error lexbuf ""