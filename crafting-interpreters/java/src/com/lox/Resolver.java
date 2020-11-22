package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final Evaluator interpreter;
    private final Stack<Map<String, Variable>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    /** The current loop nesting depth. Keeps track of the number of enclosing loops. */
    private int loopDepth = 0;

    Resolver(Evaluator interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private static class Variable {
        final Token name;
        final int slot;
        VariableState state;

        private Variable(Token name, int slot) {
            this.name = name;
            this.slot = slot;
            state = VariableState.DECLARED;
        }

        private enum VariableState {
            DECLARED,
            DEFINED,
            READ
        }
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.guard);
        resolve(expr.then);
        resolve(expr.elseBranch);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        resolveFunction(expr, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty()
                && scopes.peek().containsKey(expr.name.lexeme)
                && scopes.peek().get(expr.name.lexeme).state == Variable.VariableState.DECLARED
        ) {
            Lox.error(expr.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name, true);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (loopDepth == 0) {
            Lox.error(stmt.keyword, "Must be inside a loop to use 'break'");
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        ++loopDepth;
        resolve(stmt.condition);
        resolve(stmt.body);
        --loopDepth;
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt.function, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        Map<String, Variable> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already variable with this name in this scope.");
        }
        scope.put(name.lexeme, new Variable(name, scope.size()));
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().get(name.lexeme).state = Variable.VariableState.DEFINED;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            resolve(stmt);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveLocal(Expr expr, Token name, boolean isRead) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            Variable variable = scopes.get(i).get(name.lexeme);
            if (variable != null) {
                if (isRead) {
                    variable.state = Variable.VariableState.READ;
                }
                interpreter.resolve(expr, scopes.size() - 1 - i, variable.slot);
                return;
            }
        }
    }

    private void resolveFunction(Expr.Function expr, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : expr.params) {
            declare(param);
            define(param);
        }
        resolve(expr.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        Map<String, Variable> scope = scopes.pop();
        for (Variable variable : scope.values()) {
            if (variable.state != Variable.VariableState.READ) {
                Lox.error(variable.name, "Local variable is not used.");
            }
        }
    }
}
