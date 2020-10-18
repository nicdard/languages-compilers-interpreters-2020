# Parsing techniques
* recursive descent parsing (top-down parser) -> GCC, Js V8, Roslyn (C#).
A recursive descent parser is a literal translation of the grammar’s rules straight into imperative code. Each rule becomes a function.
* LL(k)
* LR(1) (bottom-up)
* LALR
* parser combinators
* Earley parsers (can handle all context-free grammars)
* Shunting-yard algorithm (mathematical infix expressions, stack based)
* Parsing expression grammar (packrat parsing)

# Error handling
## Synchronize
As soon as the parser detects an error, it enters panic mode. It knows at least one token doesn’t make sense given its current state in the middle of some stack of grammar productions.

Before it can get back to parsing, it needs to get its state and the sequence of forthcoming tokens aligned such that the next token does match the rule being parsed. This process is called synchronization.

To do that, we select some rule in the grammar that will mark the synchronization point. The parser fixes its parsing state by jumping out of any nested productions until it gets back to that rule. Then it synchronizes the token stream by discarding tokens until it reaches one that can appear at that point in the rule.

Any additional real syntax errors hiding in those discarded tokens aren’t reported, but it also means that any mistaken cascaded errors that are side effects of the initial error aren’t falsely reported either, which is a decent trade-off.

The traditional place in the grammar to synchronize is between statements.

### Implementation notes:
With recursive descent, the parser’s state—which rules it is in the middle of recognizing—is not stored explicitly in fields. Instead, we use Java’s own call stack to track what the parser is doing. Each rule in the middle of being parsed is a call frame on the stack. In order to reset that state, we need to clear out those call frames.

The natural way to do that in Java is exceptions. When we want to synchronize, we throw that ParseError object. Higher up in the method for the grammar rule we are synchronizing to, we’ll catch it.
## Error production
Another way to handle common syntax errors is with error productions. You augment the grammar with a rule that successfully matches the erroneous syntax. The parser safely parses it but then reports it as an error instead of producing a syntax tree.

Error productions work well because you, the parser author, know how the code is wrong and what the user was likely trying to do. That means you can give a more helpful message to get the user back on track, like, “Unary ‘+’ expressions are not supported.” Mature parsers tend to accumulate error productions like barnacles since they help users fix common mistakes.

 
