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


