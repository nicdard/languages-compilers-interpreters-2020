type unop =
  | OpNeg

type binop =
  | OpPlus
  | OpMinus
  | OpTimes
  | OpDiv

type expr =
  | ELiteral of int
  | EUnOp of unop * expr
  | EBinOp of expr * binop * expr