(* Exercise 1 *)
let swap_adjacent s =
  let l = String.length s
  in let buffer = Buffer.create l
  in let rec step s index =
    if (index + 2) > l then
      buffer
    else
      (
        Buffer.add_char buffer s.[index + 1];
        Buffer.add_char buffer s.[index];
        step s (index + 2)
      )
  in step s 0 |> Buffer.contents

(* Exercise 2 *)
let print_pascal k =
  let option_sum el o = match o with
    | None -> el
    | Some a -> a + el
  in let rec pascal_row index acc =
    if (index > k) then     
      acc
    else 
      let (prev::tail) = acc
      in let next = (List.mapi (fun index el -> (
        List.nth_opt prev (index + 1)
        |> option_sum el
      )) prev)
      in pascal_row (index + 1) ((1::next)::acc)
  in pascal_row 2 [[1]] 
  |> List.rev
  |> List.iter (fun row -> (
    List.iter (fun it -> print_int it; print_string " ") row;
    print_endline "";
  ))
 
(* Exercise 3 *)
let rec power_set = function
  | [] -> [[]]
  | x::xs ->
    let ps = power_set xs
    in ps @ List.map (fun ss -> x :: ss) ps

let rec repeat n el =
  match n with
  | 0 -> []
  | _ -> el::(repeat (n-1) el) 

let count_change d n =
  let sum l = List.fold_left (fun p next -> p + next) 0 l
  in let list_compare l1 l2 =
    let diff = List.length l1 - List.length l2
    in match diff with
    | 0 -> (List.fold_left2 (fun a b c -> match b - c with
      | d when a == 0 -> d
      | _ -> a
    ) 0 l1 l2)
    | _ -> diff
  in List.filter ((>) n) d
  |> List.map (fun it -> repeat (n / it) it)
  |> List.flatten
  |> power_set
  |> List.filter (fun it -> n == (sum it))
  |> List.map (List.sort (fun a b -> a - b))
  |> List.sort_uniq list_compare
  |> List.length