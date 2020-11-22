package com.lox;

import com.lox.ast.AstPrinter;
import com.lox.ast.AstRPNPrinter;
import com.lox.ast.Stmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static Evaluator evaluator;
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private static final String USAGE_MESSAGE = "Usage: jlox [mode] [script]\n" +
            "where script is optional and mode should be one of the following:\n" +
            "-i: run the REPL, if a script is given it is executed before displaying the prompt\n" +
            "-run: runs the given script\n" +
            "-ast: prints the abstract syntax tree\n" +
            "-astRPN: prints the AST in reverse polish notation style\n";

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            System.out.println(USAGE_MESSAGE);
            /* EX_USAGE (64) The command was used incorrectly, e.g., with the wrong number of arguments, a bad flag, a bad syntax in a parameter, or whatever.*/
            System.exit(64);
        } else if (args.length == 2) {
            String path = args[1];
            switch (args[0]) {
                case "-run":
                    evaluator = new Interpreter();
                    runFile(path);
                    break;
                case "-i":
                    if (args[1] != null) {
                        System.out.println("Ignoring script parameter.");
                    }
                    evaluator = new Interpreter();
                    runPrompt();
                    break;
                case "-ast":
                    evaluator = new AstPrinter();
                    runFile(path);
                    break;
                case "-astRPN":
                    evaluator = new AstRPNPrinter();
                    runFile(path);
                    break;
                default:
                    System.out.println("Unknown option " + args[1] + ".");
                    System.out.println(USAGE_MESSAGE);
                    System.exit(64);
            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "-i":
                    runPrompt();
                    break;
                case "-ast":
                case "-astRPN":
                case "-run":
                    System.out.println("Expected [script] for option " + args[0] + ".");
                default:
                    System.out.println("Unknown option " + args[1] + ".");
                    System.out.println(USAGE_MESSAGE);
                    System.exit(64);
            }
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            /* EX_DATAERR (65) The input data was incorrect in some way. This should only be used for user's data and not system files. */
            System.exit(65);
        }
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            // We want the REPL to keep run, just stop the execution of the previous line.
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;
        Resolver resolver = new Resolver(evaluator);
        resolver.resolve(statements);
        if (hadError) return;
        evaluator.interpret(statements);
    }

    // TODO: show the offending line and the column (character) within the line
    // TODO FEATURE: move the error reporting to a separate interface and pass an obj to scanner and parser?
    public static void error(int line, String message) {
        report(line, "", message);
    }
    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end", message);
        } else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(
                error.getMessage()
                + "\n[line " + error.token.line + "]"
        );
        hadRuntimeError = true;
    }

    /**
     * Pretty prints an error to the user, giving information about line, place and what happened.
     * Note: It’s a good engineering practice to separate the code that generates the errors from the code that reports them.
     * @param line
     * @param where
     * @param message
     */
    private static void report(
        int line,
        String where,
        String message
    ) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }
}