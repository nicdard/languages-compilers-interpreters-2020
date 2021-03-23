(* Exercise 1 *)
(**
Calculate e^x using the approximation 1 + x + x^2/2 + x^3/3! ...
@param x 
@param n
**)
let series_expansion (n: int) (x: int) =
  let rec integer_x_exp n = 
    match n with
    | 0 -> 1
    | m -> x * integer_x_exp (m - 1)
  in let fact n =
    let rec helper_fact acc n = 
      match n with
      | 0 -> acc
      | m -> helper_fact (acc * n) (m - 1)
    in helper_fact 1 n
  in let rec helper_series_expansion acc n = 
    match n with
    | 0 -> acc
    | m -> helper_series_expansion (acc + (integer_x_exp n) / (fact n)) (n - 1)
  in helper_series_expansion 0 n;;

let sol_ex1 = series_expansion 10;;

(* Exercise 2 *)
(**
Super digit of an integer x is defined as follows:
  1. if x has only 1 digit, then its super digit is x;
  2. otherwise, the super digit of x is equal to the super digit of the sum of its digits.
@param x
@return the super digit of x
**)
let super_digit x: int =
  let digits x =
    let rec helper_digits acc x =
      match x with
      | a when x >= 0 && x <= 9 -> a::acc
      | m when x > 9 -> helper_digits (m mod 10::acc) (m / 10)
      | n -> helper_digits acc (abs x)
    in helper_digits [] x
  in let sum_list l = 
    List.fold_left (fun acc next -> acc + next) 0 l
  in let rec helper_super_digit x =
    match x with
    | m when x >= 0 && x <= 9 -> m
    | n -> helper_super_digit (sum_list (digits n))
  in helper_super_digit x;;


(* Exercise 3 *)
(**
@param l
@param n
@return a new list from l where each element is replicated n times.
**)
let list_replication (l: 'a list) (n: int): 'a list =
  let repeat el times =
    let rec helper_repeat acc el times =
      match times with
      | n when times > 0 -> helper_repeat (el::acc) el (times - 1)
      | _ -> acc
    in helper_repeat [] el times
  in List.concat_map (fun i -> repeat i n) l;;

(* Exercise 4 *)
(**
@param l
@param n
@return a list obtained by concatenating l with itself n times.
**)
let list_replication1 l n =
  let rec helper_list_replication1 acc l n =
    match n with
    | n when n > 0 -> helper_list_replication1 (l::acc) l (n - 1)
    | _ -> acc
  in List.flatten (helper_list_replication1 [] l n);;

(* Exercise 5 *)
(**
@param r
@return true if r represents a function from int to int.
**)
let is_function (r: (int * int) list): bool =
  let rec verify acc (r: (int * int) list): bool =
    match r with
    | (f1, s1)::(f2, s2)::xs -> begin 
      if f1 = f2 
        then verify ((s1 = s2) && acc) ((f2, s2)::xs)
        else verify acc ((f2, s2)::xs)
      end
    | _ -> acc
  in List.sort (fun (f1, s1) (f2, s2) -> f1 - f2) r
  |> verify true;;


(* Exercise 6 *)
(**
@param p
@param q
@return Return a string obtained by iterleaving 
**)
let mangle_string p q =
  let chars_of_string s =
    let rec helper_chars_of_string index l =
      if index < 0 then l else helper_chars_of_string (index - 1) (s.[index]::l) 
    in helper_chars_of_string (String.length s - 1) []
  in let (p1, q1) = (chars_of_string p, chars_of_string q)
  in List.combine p1 q1
  |> List.map (fun (f, s) -> [f;s])
  |> List.flatten
  |> fun chars -> 
    begin
      let buf = Buffer.create 16 
      in List.iter (Buffer.add_char buf) chars;
      Buffer.contents buf
    end;;

