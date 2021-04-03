type unop =
  | OpNeg

type binop =
  | OpPlus
  | OpMinus
  | OpTimes
  | OpDiv

type expr =
  | ELiteral of int
  | Let of string * expr
  | Var of string
  | EUnOp of unop * expr
  | EBinOp of expr * binop * expr
