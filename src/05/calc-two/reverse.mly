%%

%public expr:
| i = INT
    { i }
| e1 = expr e2 = expr PLUS
    { e1 + e2 }
| e1 = expr e2 = expr MINUS
    { e1 - e2 }
| e1 = expr e2 = expr TIMES
    { e1 * e2 }
| e1 = expr e2 = expr DIV
    { e1 / e2 }
