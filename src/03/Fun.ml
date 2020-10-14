(**
   An example of an interpreter for a small strongly typed functional language.
 *)

(** Syntax of the language *)
type expr =
  | CstI of int
  | CstB of bool
  | Var of string
  | Let of string * expr * expr
  | Prim of string * expr * expr
  | If of expr * expr * expr
  | LetFun of string * string * expr * expr    (* (f, x, fBody, letBody) *)
  | Call of expr * expr
  | CstTuple of expr * expr * expr
  | Proj of expr * expr
  | Empty 
  | Cons of expr * expr
  | Head of expr
  | Tail of expr

(** Definition of environment. An environment is a map from identifier to "something".
   In the semantics this something is a value (what the identifier is bound to).
   In the type system this "something" is a type.
   For simplicity we represent the environment as an association list, i.e., a list of pair (identifier, data).
 *)
type 'v env = (string * 'v) list

(**
 Expressible and Denotable values.
 A runtime value is an integer or a function closure
 Boolean are encoded as integers.
*)
type value = 
  | Int of int
  | Tuple of value * value * value
  | List of value list
  | Closure of string * string * expr * value env

(**
   Given an environment {env} and an identifier {x} it returns the data {x} is bound to.
   If there is no binding, it raises an exception.
 *)
let rec lookup (x: string) (env: value env): value =
  match env with
  | ((var, e)::es) -> if (var = x) then e else lookup x es
  | _ -> failwith ("Unbound name " ^ x)

(** Interpreter for expression. Given an expression {e} and an enviroment {env} that closes {e},
   this function evaluates {e} and returns the result of the computation.
   Note this function implements the big-step operational semantics with environment.
 *)
let rec eval (e: expr) (env: value env): value =
  match e with
  | CstI i -> Int i
  | CstB b -> Int (if b then 1 else 0)
  | Var x -> lookup x env
  | If (guard, th, el) -> let g = eval guard env 
    in (match g with
    | Int 0 -> eval el env
    | Int _ -> eval th env
    | _ -> failwith "Illegal If guard value, it must be an Int value"
    )
  | Prim (op, e1, e2) -> 
    let v1 = eval e1 env
    in let v2 = eval e2 env
    in (match op, v1, v2 with 
    | "+", Int i1, Int i2 -> Int (i1 + i2)
    | "-", Int i1, Int i2 -> Int (i1 - i2)
    | "*", Int i1, Int i2 -> Int (i1 * i2)
    | "/", Int i1, Int i2 -> Int (i1 / i2)
    | "=", Int i1, Int i2 -> Int (if i1 = i2 then 1 else 0)
    | "<", Int i1, Int i2 -> Int (if i1 < i2 then 1 else 0)
    | _ -> failwith ("Unknwon operation, expected one of {+,-,*,/} but found " ^ op)
    )
  | Let (name, eRhs, body) -> 
    let v = eval eRhs env
    in let env2 = (name, v) :: env
    in eval body env2
  | LetFun (fName, par, fBody, letBody) -> 
    let c = Closure (fName, par, fBody, env) 
    in let letEnv = (fName, c) :: env
    in eval letBody letEnv
  | Call (eFun, eArg) -> let closure = eval eFun env in
    ( match closure with
      | Closure (fName, parName, fBody, fEnv) -> 
        let paramVal = eval eArg env
        in let fEvalEnv = (parName, paramVal) :: (fName, closure) :: fEnv
        in eval fBody fEvalEnv
      | _ -> failwith "The expression is not callable"
    )
  | CstTuple (e1, e2, e3) ->
    let val1 = eval e1 env
    in let val2 = eval e2 env
    in let val3 = eval e3 env
    in Tuple (val1, val2, val3)
  | Proj (t, i) ->
    let tuple = eval t env
    in let ith = eval i env
    in ( match tuple, ith with
      | Tuple (v1, v2, v3), Int index -> (
        match index with
        | 1 -> v1
        | 2 -> v2
        | 3 -> v3
        | _ -> failwith "Projection: index out of bound exception"
        )
      | _ -> failwith "Invalid projecton call, the first argument must be a tuple and the second a number representing the index"
    )
  | Empty -> List []
  | Cons (h, t) -> 
    let head = eval h env
    in let tail = eval t env
    in (match head, tail with
      | _, List [] -> List (head::[])
      | Int _, List ((Int i)::l) -> List (head::(Int i)::l)
      | Tuple _, List (Tuple (v1, v2, v3)::l) -> List (head::(Tuple (v1, v2, v3))::l)
      | Closure _, List (Closure (a, b, c, d)::l) -> List (head::(Closure (a, b, c, d))::l)
      | _ -> failwith "Invalid type: a list is an homogeneous sequence of values"
    )
  | Head l -> (match (eval l env) with
    | List (h::tail) -> h
    | List [] -> failwith "Cannot get the head of an empty list"
    | _ -> failwith "Cannot apply head on a type that is not list"
  )
  | Tail l -> (match (eval l env) with
    | List [] -> failwith "Cannot get the tail from an empty list"
    | List (h::tail) -> List tail
    | _ -> failwith "Cannot apply tail on a type that is not list" 
  )

(* Evaluate in empty environment: program must have no free variables: *)
let run e = eval e []