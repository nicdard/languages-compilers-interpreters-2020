package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
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
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS)) return classDeclaration();
            if (check(TokenType.FUN) && checkNext(TokenType.IDENTIFIER)) {
                consume(TokenType.FUN, null);
                return function("function");
            }
            if (match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch(ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");
        Expr.Variable superclass = null;
        if (match(TokenType.LESS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(previous());
        }
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> classMethods = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.CLASS)) {
                // static method
                classMethods.add(function("static method"));
            } else {
                methods.add(function("method"));
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, superclass, methods, classMethods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        return new Stmt.Function(name, functionBody(kind));
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.BREAK)) return breakStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        consume(TokenType.SEMICOLON, "Expected ';' after 'break'.");
        return new Stmt.Break(keyword);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'.");
        Stmt initializer = null;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");
        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after loop clauses.");
        Stmt body = statement();
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(
                    initializer,
                    body
            ));
        }
        return body;
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = ternary();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr ternary() {
        Expr expression = comma();
        if (match(TokenType.QUESTION_MARK)) {
            Expr then = expression();
            consume(TokenType.COLON, "Expect ':' after expression.");
            Expr elseBranch = expression();
            expression = new Expr.Ternary(expression, then, elseBranch);
        }
        return expression;
    }

    private Expr comma() {
        Expr expression = or();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expr right = or();
            expression = new Expr.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expr or() {
        Expr expr = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments in a function call.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(TokenType.THIS)) return new Expr.This(previous());
        if (match(TokenType.FUN)) return functionBody("anonymous function");
        if (match(TokenType.SUPER)) {
            Token keyword = previous();
            consume(TokenType.DOT, "Expect '.' after 'super'");
            Token method = consume(
                    TokenType.IDENTIFIER,
                    "Expect superclass method name."
            );
            return new Expr.Super(keyword, method);
        }
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Unexpected expression.");
    }

    private Expr.Function functionBody(String kind) {
        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() > 255) {
                    error(peek(), "Can't have more then 255 parameters.");
                }
                parameters.add(
                        consume(TokenType.IDENTIFIER, "Expect parameter name.")
                );
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Expr.Function(parameters, body);
    }

    /**
     * Consumes and return the current token if it matches the expected type,
     * signals and throws an {@link ParseError} otherwise.
     * @param type the expected type.
     * @param message the message to be shown to the user if an error occurs.
     * @return the current token.
     */
    private Token consume(final TokenType type, final String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    /**
     * Signals an error to the user.
     * @param token the unexpected token.
     * @param message the message for the user.
     * @return a new ParseError to
     */
    private ParseError error(final Token token, final String message) {
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
    private boolean check(final TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    /**
     * Performs a lookahead of length 2 without consuming the tokens.
     * @param tokenType
     */
    private boolean checkNext(final TokenType tokenType) {
        if (isAtEnd()) return false;
        if (tokens.get(current + 1).type == TokenType.EOF) return false;
        return tokens.get(current + 1).type == tokenType;
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
