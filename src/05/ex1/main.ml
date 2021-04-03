open Syntax

type 'v env = (string * 'v) list

let rec lookup (x: string) (env: int env) =
  match env with
  | ((var, e)::es) -> if var = x then e 
                      else lookup x es
  | _ -> failwith (Printf.sprintf "Runtime error: Unbound name '%s'.\n%!" x)

let interpret_env (e: expr) (env: int env) =
  let rec interpret (e: expr) =
    match e with
    | Var x                     -> lookup x env
    | ELiteral i                -> i
    | EBinOp (e1, OpPlus, e2)   -> interpret e1 + interpret e2
    | EBinOp (e1, OpMinus, e2)  -> interpret e1 - interpret e2
    | EBinOp (e1, OpTimes, e2)  -> interpret e1 * interpret e2
    | EBinOp (e1, OpDiv, e2)    -> interpret e1 / interpret e2
    | EUnOp (OpNeg, e)          -> - (interpret e)
    | _                         -> failwith "Shouldn't happen"
  in interpret e

let eval (e: expr) (env: int env) =
  match e with
  | Let (id, e)               -> let value = interpret_env e env in
                                  (Printf.sprintf "val %s : %d" id value, (id, value)::env)
  | _                         -> (Printf.sprintf "%d" (interpret_env e env), env) 
  
let process (line: string) env =
  let linebuf = Lexing.from_string line in
  try
    let value, env = eval (Parser.main Lexer.token linebuf) env in
    Printf.printf "%s\n%!" value;
    env
  with
    | Lexer.Error msg ->
        Printf.eprintf "%s\n%!" msg;
        env
    | Parser.Error ->
        Printf.eprintf "Syntax error at offset %d.\n%!" (Lexing.lexeme_start linebuf); 
        env

let process (optional_line: string option) env =
  match optional_line with
  | None -> env
  | Some line -> process line env
  
let rec repeat channel env =
  let optional_line, continue = Lexer.line channel in
  let env = process optional_line env in
  if continue then
    repeat channel env

let () =
  repeat (Lexing.from_channel stdin) []
