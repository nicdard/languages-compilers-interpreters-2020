type register = 
  | R of int
  [@@deriving show]

let make_register (index: int ref) = 
  let r = R !index in
  incr index;
  r

type unary_op =
  | Load
  [@@deriving show]

type binary_op =
  | Add 
  | Sub 
  | Mul 
  | Div 
  [@@deriving show]

type instruction =
  | IUnOp of unary_op * register * int
  | IBinOp of binary_op * register * register * register
  [@@deriving show]

let string_of_register (R i) = Printf.sprintf "r%d" i 

let string_of_instruction = function
  | IUnOp(i, r, n) -> Printf.sprintf "%s %s %d" (show_unary_op i) (string_of_register r) n
  | IBinOp(i, res, f, s) -> Printf.sprintf "%s %s %s %s" 
    (show_binary_op i) 
    (string_of_register res)
    (string_of_register f)
    (string_of_register s) 
  
