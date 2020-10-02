(* Exercise 1 *)

let rec integer_pow x n =
  match n with
  | 0 -> 0
  | 1 -> x
  | n -> 
    let b = integer_pow x (n / 2) 
    in b * b * (if n mod 2 = 0 then 1 else x)

(**
@param x n
@return the series expansion of e^x using the first n terms. **)
let rec series_exp x n = 
  match n with
  | 0 -> 1
  | _ -> ((integer_pow x n) / n) + series_exp x (n-1)

          
(* Exercise 2 *)
let digits x = 
  let rec loop acc n =
    match n with
    | 0 -> acc
    | _ -> loop (n mod 10::acc) (n / 10) in
  match x with
  | 0 -> [0]
  | _ -> loop [] x

let rec super_digit x = if (x > -10 && x < 10) 
  then x
  else super_digit (List.fold_left 
    (fun acc d -> acc + d)
    0
    (digits x)
  ) 


(* Exercise 3 *)
let list_replication l n =
  let rec replicate nn acc el =
    match nn with
    | 0 -> acc
    | _ -> replicate (nn - 1) (el::acc) el
  in List.flatten (List.map (replicate n []) l)

(* Exercise 4 *)
let list_replication1 l n =
  let rec loop acc n =
    match n with
    | 0 -> acc
    | _ -> loop (acc@l) (n - 1)
  in loop [] n

(* Exercise 5 *)

(* returns a list where consecutive duplicate elements are removed *)
let rec compress = function
    | a :: (b :: _ as t) -> if a = b then compress t else a :: compress t
    | smaller -> smaller

let is_function r =
  let l = List.map (fun (f, s) -> f) r
  |> List.sort (fun a b -> a - b)
  |> compress
  in List.length r == List.length l
  
(* Exercise 6 *)
let rec zip2 l1 l2 =
  match (l1, l2) with
  | ([], []) -> []
  | (x::xs, y::ys) -> (x, y)::(zip2 xs ys)
  | _, _ -> [] (* Cover different length lists case *)

let stringMap f acc s =
  let rec stringMapIndexed f acc s index = 
    if index < 0 then
      acc
    else
      stringMapIndexed f (f acc s.[index]) s (index - 1)
  in stringMapIndexed f acc s ((String.length s) - 1)

let string_of_chars chars = 
  let buf = Buffer.create 16 in
  List.iter (Buffer.add_char buf) chars;
  Buffer.contents buf

let mingle_string p q =
  let chars = stringMap (fun acc next -> next::acc) []
  in let zipped = zip2 (chars p) (chars q)
  in let splitted = List.flatten (List.map (fun (f, s) -> [f;s]) zipped)
  in string_of_chars splitted

  