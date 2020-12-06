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
    private ClassType currentClass = ClassType.NONE;
    /** The current loop nesting depth. Keeps track of the number of enclosing loops. */
    private int loopDepth = 0;

    Resolver(Evaluator interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
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

        private Variable(Token name, int slot, VariableState state) {
            this.name = name;
            this.slot = slot;
            this.state = state;
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
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
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
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.object);
        resolve(expr.value);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword, true);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword, true);
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
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);
        if (stmt.superclass != null
            && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)
        ) {
            Lox.error(
                    stmt.superclass.name,
                    "A class can't inherit from itself"
            );
        }
        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            beginScope();
            Map<String, Variable> scope = scopes.peek();
            scope.put("super", new Variable(stmt.superclass.name, scope.size(), Variable.VariableState.READ));
            resolve(stmt.superclass);
        }
        beginScope();
        Map<String, Variable> scope = scopes.peek();
        scope.put("this", new Variable(stmt.name, scope.size(), Variable.VariableState.READ));
        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method.function, declaration);
        }
        for (Stmt.Function method : stmt.classMethods) {
            beginScope();
            scopes.peek().put("this", new Variable(stmt.name, scope.size(), Variable.VariableState.READ));
            resolveFunction(method.function, FunctionType.METHOD);
            endScope();
        }
        endScope();
        if (stmt.superclass != null) endScope();
        currentClass = enclosingClass;
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
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
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
