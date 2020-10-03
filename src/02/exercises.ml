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
(*
let count_change d n =
  let valid = List.filter 
  *)    
