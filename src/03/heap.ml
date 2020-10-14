type 'a t = 
  | Leaf
  | Node of 'a * int * 'a t * 'a t

let empty = Leaf

let singleton k = Node (k, 1, Leaf, Leaf)

let get_min heap = match heap with
  | Leaf -> None
  | Node (n, _, _, _) -> Some n

let rec merge h1 h2 = match h1, h2 with
  | Leaf, _ -> h2
  | _, Leaf -> h1
  | Node (n1, sv1, l1, r1), Node (n2, sv2, l2, r2) -> 
    if (n1 <= n2) then
      let sub = merge r1 h2
      in match l1, sub with
        | _, Leaf -> l1
        | Leaf, Node (_, _, _, _) -> Node (n1, 1, sub, Leaf)
        | Node (ll, svl1, ll1, rr1), Node (n, sv, l, r) -> if (sv > svl1) then
            Node (n1, sv1, sub, l1)
          else Node (n1, (svl1 + 1), l1, sub)
    else
      let sub = merge r2 h1
      in match l2, sub with
        | _, Leaf -> l2
        | Leaf, Node (_, _, _, _) -> Node (n2, 1, sub, Leaf)
        | Node (ll, svl1, ll1, rr1), Node (n, sv, l, r) -> if (sv > svl1) then
            Node (n2, sv2, sub, l2)
          else Node (n2, (svl1 + 1), l2, sub)

let insert k heap =
  let new_heap = singleton k
  in merge heap new_heap

let delete_min heap = match heap with
  | Leaf -> Leaf
  | Node (m, sv, l, r) -> merge l r
