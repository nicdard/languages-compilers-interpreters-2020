package com.lox;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final Object literal;

    public Token(TokenType type, String lexeme, int line, Object literal) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.literal = literal;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + (literal != null ? literal : "");
    }
}

/*
 * Tips: Some token implementations store the location as two numbers: the offset from the beginning of the source
 * file to the beginning of the lexeme, and the length of the lexeme. The scanner needs to know these anyway, so
 * there’s no overhead to calculate them.
 *
 * An offset can be converted to line and column positions later by looking back at the source file and counting the
 * preceding newlines. That sounds slow, and it is. However, you only need to do it when you need to actually display
 * a line and column to the user. Most tokens never appear in an error message. For those, the less time you spend
 * calculating position information ahead of time, the better.
 */

/*
 * Tokens are not entirely homogeneous either. Tokens for literals store the value but other kinds of lexemes don’t
 * need that state. I have seen scanners that use different classes for literals and other kinds of lexemes, but I
 * figured I’d keep things simpler.
 */
