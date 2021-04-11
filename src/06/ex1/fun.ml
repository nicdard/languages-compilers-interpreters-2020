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
  | Letfun of string * string * ttype *  expr * expr    (* (f, x, type fBody, letBody) *)
  | Call of expr * expr
and ttype =
  | Tint
  | Tbool
  | Tfun of ttype * ttype

(** Definition of environment. An environment is a map from identifier to "something".
   In the semantics this something is a value (what the identifier is bound to).
   In the type system this "something" is a type.
   For simplicity we represent the environment as an association list, i.e., a list of pair (identifier, data).
 *)

type 'v env = (string * 'v) list

(**
   Given an environment {env} and an identifier {x} it returns the data {x} is bound to.
   If there is no binding, it raises an exception.
 *)
let rec lookup env x =
    match env with
    | []        -> failwith (x ^ " not found")
    | (y, v)::r -> if x=y then v else lookup r x

(**
 Expressible and Denotable values.
 A runtime value is an integer or a function closure
 Boolean are encoded as integers.
*)
type value =
  | Int of int
  | Closure of string * string * expr * value env       (* (f, x, fBody, fDeclEnv) *)

let string_of_value = function
  | Int i ->
      Printf.sprintf "<int>: %d" i
  | Closure(fname, _, _, _) -> 
      Printf.sprintf "<fun>: %s" fname 

(** Interpreter for expression. Given an expression {e} and an enviroment {env} that closes {e},
   this function evaluates {e} and returns the result of the computation.
   Note this function implements the big-step operational semantics with environment.
 *)
let rec eval (e : expr) (env : value env) : value =
    match e with
    | CstI i -> Int i
    | CstB b -> Int (if b then 1 else 0)
    | Var x  -> lookup env x
    | Prim(ope, e1, e2) ->
      let v1 = eval e1 env in
      let v2 = eval e2 env in
      begin
      match (ope, v1, v2) with
      | ("*", Int i1, Int i2) -> Int (i1 * i2)
      | ("/", Int i1, Int i2) -> Int (i1 / i2)
      | ("+", Int i1, Int i2) -> Int (i1 + i2)
      | ("-", Int i1, Int i2) -> Int (i1 - i2)
      | ("=", Int i1, Int i2) -> Int (if i1 = i2 then 1 else 0)
      | ("<", Int i1, Int i2) -> Int (if i1 < i2 then 1 else 0)
      |  _ -> failwith "unknown primitive or wrong type"
      end
    | Let(x, eRhs, letBody) ->
      let xVal = eval eRhs env in
      let letEnv = (x, xVal) :: env in
      eval letBody letEnv
    | If(e1, e2, e3) ->
      begin
      match eval e1 env with
      | Int 0 -> eval e3 env
      | Int _ -> eval e2 env
      | _     -> failwith "eval If"
      end
    | Letfun(f, x, _, fBody, letBody) ->
      let bodyEnv = (f, Closure(f, x, fBody, env)) :: env in
      eval letBody bodyEnv
    | Call(eFun, eArg) ->
      let fClosure = eval eFun env in
      begin
      match fClosure with
      | Closure (f, x, fBody, fDeclEnv) ->
        let xVal = eval eArg env in
        let fBodyEnv = (x, xVal) :: (f, fClosure) :: fDeclEnv
        in eval fBody fBodyEnv
      | _ -> failwith "eval Call: not a function"
      end

(* Evaluate in empty environment: program must have no free variables: *)

let run e = eval e []

(* The type environment we start with. 
  It contains the type of primitives operators and of other primitive functions *)

let prelude = [
  "+", Tfun(Tint, Tfun(Tint, Tint));
  "-", Tfun(Tint, Tfun(Tint, Tint));
  "*", Tfun(Tint, Tfun(Tint, Tint));
  "/", Tfun(Tint, Tfun(Tint, Tint));
  "<", Tfun(Tint, Tfun(Tint, Tint));  
]

exception Type_error of string

let rec type_of gamma e =
  match e with
  | CstI(_) -> Tint
  | CstB(_) -> Tbool
  | Var(x) -> lookup gamma x
  | Let(x, e1, e2) ->
    let t1 = type_of gamma e1 in
    type_of ((x, t1) :: gamma) e2
  | If(g, e1, e2) ->
    if (type_of gamma g) = Tbool then
      let t1 = type_of gamma e1 in
      let t2 = type_of gamma e2 in
      if t1 = t2 then t1 else raise (Type_error "if branches have different types")
    else raise (Type_error "if guard is not of type boolean")
  | Letfun(fname, formal, (Tfun(t1, t2) as t), body, e) ->
    let gamma' = (fname, t) :: (formal, t1) :: gamma in
    if (type_of gamma' body) = t2 then
      type_of ((fname, t) :: gamma) e
    else
      raise (Type_error ("Wrong return type for function " ^ fname))
  | Prim("=", e1, e2) -> (* This is handled apart from the others because it is a polymorphic operator *)
    let t1 = type_of gamma e1 in
    let t2 = type_of gamma e2 in
    begin
      match (t1, t2) with
      | Tint, Tint
      | Tbool, Tbool -> Tbool
      | Tfun(_,_), Tfun(_,_) ->
        raise (Type_error "Error comparing functional values for equality")
      | _, _ -> raise (Type_error "The operator '=' expects two expressions of the same type")
    end
  | Prim(op, e1, e2) ->
    let t1 = type_of gamma e1 in
    let t2 = type_of gamma e2 in
    let t3 = lookup gamma op in
    begin
      match t3 with
      | Tfun(t1', Tfun(t2', tr')) ->
        if (t1' = t1 && t2' = t2) then
          tr'
        else raise (Type_error ("error in the arguments of " ^ op))
      | _ -> failwith "Inconsistent state"
    end
  | Call(e1, e2) ->
    let t1 = type_of gamma e1 in
    let t2 = type_of gamma e2 in
    begin
      match t1 with
      | Tfun(tx, tr) ->
        if (tx = t2) then
          tr
        else
          raise (Type_error "Functional application: argument type mismatch")
      | _ -> raise (Type_error "Cannot apply a non functional value")
    end
  | _ -> assert false

let type_check e = type_of prelude e
