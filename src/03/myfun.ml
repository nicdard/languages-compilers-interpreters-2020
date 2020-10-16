(** Syntax of the language **)
type exp =
  | CstInt of int
  | CstTrue
  | CstFalse
  | Times of exp * exp
  | Sum of exp * exp
  | Sub of exp * exp
  | Eq of exp * exp
  | Iszero of exp
  | Or of exp * exp 
  | And of exp * exp
  | Not of exp
  | Ifthenelse of exp * exp * exp

(** Expressible types: Intuitively, the constructors are the abstract representation 
of the runtime descriptors specifying the type information of data entities **)
type evT =
  | Int of int
  | Bool of bool

let typecheck ((x, y) : (string * evT)) : bool = match x with
  | "int" ->  (match y with
    | Int u -> true
    | _ -> false  
  )
  | "bool" -> (match y with
    | Bool u -> true
    | _ -> false
  )
  | _ -> failwith ("Invalid type: " ^ x)

let is_zero x = match (typecheck("int",x), x) with
  | (true, Int(y)) -> Bool(y=0)
  | (_, _) -> failwith("run-time error")

let int_eq(x,y) =
  match (typecheck("int",x), typecheck("int",y), x, y) with
  | (true, true, Int(v), Int(w)) -> Bool(v = w)
  | (_,_,_,_) -> failwith("run-time error ")

let int_plus(x, y) =
  match (typecheck("int",x), typecheck("int",y), x, y) with
  | (true, true, Int(v), Int(w)) -> Int(v + w)
  | (_,_,_,_) -> failwith("run-time error ")

let int_times(x, y) =
  match (typecheck("int",x), typecheck("int",y), x, y) with
  | (true, true, Int(v), Int(w)) -> Int(v * w)
  | (_,_,_,_) -> failwith("run-time error ")

let int_sub(x, y) =
  match (typecheck("int",x), typecheck("int",y), x, y) with
  | (true, true, Int(v), Int(w)) -> Int(v - w)
  | (_,_,_,_) -> failwith("run-time error ")
  
let bool_and(x, y) =
  match (typecheck("bool",x),typecheck("bool",y), x, y) with
  | (true, true, Bool(v), Bool(w)) -> Bool(v && w)
  | (_,_,_,_) -> failwith("run-time error ")
  
let bool_or(x, y) =
  match (typecheck("bool",x),typecheck("bool",y), x, y) with
  | (true, true, Bool(v), Bool(w)) -> Bool(v || w)
  | (_,_,_,_) -> failwith("run-time error ")
  
let bool_not(x) =
  match (typecheck("bool",x), x) with
  | (true, Bool(v)) -> Bool(not(v))
  | (_,_) -> failwith("run-time error ")

let rec eval e =
  match e with
  | CstInt(n) -> Int(n)
  | CstTrue -> Bool(true)
  | CstFalse -> Bool(false)
  | Iszero(e1) -> is_zero(eval(e1))
  | Eq(e1, e2) -> int_eq(eval(e1), eval(e2))
  | Times(e1,e2) -> int_times(eval(e1), eval(e2))
  | Sum(e1, e2) -> int_plus(eval(e1), eval(e2))
  | Sub(e1, e2) -> int_sub(eval(e1), eval(e2))
  | And(e1, e2) -> bool_and(eval(e1), eval(e2))
  | Or(e1, e2) -> bool_or(eval(e1), eval(e2))
  | Not(e1) -> bool_not(eval(e1))
  | Ifthenelse(e1,e2,e3) -> let g = eval(e1) in (
    match (typecheck("bool", g), g) with
      | (true, Bool(true)) -> eval(e2)
      | (true, Bool(false)) -> eval(e3)
      | (_, _) -> failwith ("nonboolean guard")
    )
  | _ -> failwith ("run-time error");;