package com.lox;

import com.lox.ast.Expr;

import java.util.List;

/* https://craftinginterpreters.com/parsing-expressions.html */
class Parser {

    /**
     * A light-weight exception used for synchronisation inside the Parser.
     */
    private static class ParseError extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * The tokens produced by the Scanner.
     * Note: the last element is always the EOF token.
     */
    private final List<Token> tokens;
    /** The index of the current token. */
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Starts recursive-descent parsing.
     */
    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expression = equality();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = comma();
            expression = new Expr.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL
        )) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Unexpected expression.");
    }

    /**
     * Consumes and return the current token if it matches the expected type,
     * signals and throws an {@link ParseError} otherwise.
     * @param type the expected type.
     * @param message the message to be shown to the user if an error occurs.
     * @return the current token.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /**
     * Signals an error to the user.
     * @param token the unexpected token.
     * @param message the message for the user.
     * @return a new ParseError to
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Discards tokens until it thinks it found a statement boundary.
     */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    /**
     * Performs a lookahead on the tokens of length 1.
     * @param types the set of token types to be tested against the current token.
     * @return true if any of the input types matches the current token type, false if none matches.
     */
    private boolean match(TokenType ...types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * @param type the expected token type.
     * @return true if the expected token type matches the type of the current token
     * and we did not reach the EOF.
     */
    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    /**
     * @return the current token and consumes it.
     */
    private Token advance() {
        if (!isAtEnd()) ++this.current;
        return previous();
    }

    /**
     * @return the current token.
     */
    private Token peek() {
        return this.tokens.get(current);
    }

    /**
     * @return true is the token is the last one in the list, i.e. EOF.
     */
    private boolean isAtEnd() {
        return this.peek().type == TokenType.EOF;
    }

    /**
     * @return the previous token.
     */
    private Token previous() {
        return this.tokens.get(current - 1);
    }
}
