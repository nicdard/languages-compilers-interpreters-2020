package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* https://craftinginterpreters.com/scanning.html 4.4 */

/**
 * Lox's Scanner.
 * It looks ahead at most two character.
 */
public class Scanner {

    private final String source;
    private List<Token> tokens = new ArrayList<>();
    /** Points to the first character of the lexeme being scanned. */
    private int start = 0;
    /** Points to the current character being scanned. */
    private int current = 0;
    /** Tracks what source file line {@link Scanner#current} is on */
    private int line = 1;
    /** Map from the keywords to their {@link TokenType} */
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",     TokenType.AND);
        keywords.put("break",   TokenType.BREAK);
        keywords.put("class",   TokenType.CLASS);
        keywords.put("else",    TokenType.ELSE);
        keywords.put("false",   TokenType.FALSE);
        keywords.put("for",     TokenType.FOR);
        keywords.put("fun",     TokenType.FUN);
        keywords.put("if",      TokenType.IF);
        keywords.put("nil",     TokenType.NIL);
        keywords.put("or",      TokenType.OR);
        keywords.put("print",   TokenType.PRINT);
        keywords.put("return",  TokenType.RETURN);
        keywords.put("super",   TokenType.SUPER);
        keywords.put("this",    TokenType.THIS);
        keywords.put("true",    TokenType.TRUE);
        keywords.put("var",     TokenType.VAR);
        keywords.put("while",   TokenType.WHILE);

    }

    public Scanner(final String source) {
        this.source = source;
    }

    /**
     * Consumes the source file and extracts tokens.
     * @return The list of tokens, ending with an {@link TokenType#EOF}.
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        this.tokens.add(new Token(
                TokenType.EOF,
                "",
                line,
                null
        ));
        return this.tokens;
    }

    /**
     * Signal the end of the source file.
     * @return true if we have reached the EOF.
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Recognizes a lexeme.
     */
    private void scanToken() {
        char c = this.advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case ':': addToken(TokenType.COLON); break;
            case '?': addToken(TokenType.QUESTION_MARK); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    // It is a comment. It goes until the end of line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    multiLineComment();
                    break;
                } else {
                    addToken(TokenType.SLASH);

                }
                break;
            case ' ':
            case '\t':
            case '\r':
                // Ignore whitespaces.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else  {
                    // We keep scanning so we can signal all the syntax errors to the user at once.
                    // TODO enhancement: The code reports each invalid character separately, so this shotguns the user with a blast of errors if they accidentally paste a big blob of weird text. Coalescing a run of invalid characters into a single error would give a nicer user experience.
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Performs a 1-lookahead on the input.
     * @return the next character of the input.
     * It doesn't consume the input, for that refer to: {@link Scanner#advance}
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return this.source.charAt(current);
    }
    /**
     * Performs a 2-lookahead on the input.
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return this.source.charAt(current + 1);
    }


    /**
     * Consumes the next character in the source file.
     * @return the next character.
     */
    private char advance() {
        ++current;
        return this.source.charAt(current - 1);
    }

    /**
     * Adds a token to the current list of tokens.
     * @param type the kind {@link TokenType} of the token
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line, literal));
    }

    /**
     * Matches the next character in the source with <code>expected</code>.
     * When and only if the character is matched, it gets also consumed.
     * @return true if <code>expected</code> is equal to the next character in the source.
     * Note that if the end of the source is reached, it returns always false.
     * Also note that we are actually performing a lookahead.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /**
     * Searches for a string literal in the input.
     * More formally, it recognises a {@link TokenType#STRING} if it finds a
     * closing double-quote in the source before the EOF is reached.
     *
     * Note: Lox supports multi-line strings.
     */
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        // Consume the closing double-quote.
        advance();
        addToken(TokenType.STRING, this.source.substring(start + 1, current - 1));
    }

    /**
     * Searches for a multi-line comment closing sequence.
     */
    private void multiLineComment() {
        // A multi-line comment
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }
        if (peek() == '*' && peekNext() == '/') {
            // Consume closing */
            advance();
            advance();
        } else {
            Lox.error(line, "Unclosed multi-line comment.");
        }
    }

    /**
     * @return true if c is a digit.
     */
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Searches for a number literal in the source.
     */
    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the dot.
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(this.source.substring(start, current)));
    }

    /**
     * @return true if c is a letter or an underscore.
     */
    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c == '_');
    }

    /**
     * @return true if c is either a letter or an underscore or a digit.
     */
    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Search for an identifier.
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String value = source.substring(start, current);
        TokenType type = keywords.get(value);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
}

/*
 * maximal munch: When two lexical grammar rules can both match a chunk of code that the scanner is looking at,
 * whichever one matches the most characters wins.
 */
