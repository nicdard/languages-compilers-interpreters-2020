package com.lox.ast;

import com.lox.Evaluator;

import java.util.List;

public class AstRPNPrinter implements Evaluator, Expr.Visitor<String>, Stmt.Visitor<String> {

    @Override
    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) {
            System.out.println(statement.accept(this));
        }
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.name.lexeme + " "
                + expr.value.accept(this) + " "
                + "=";
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return expr.guard.accept(this) + " "
                + expr.then.accept(this) + " "
                + expr.elseBranch.accept(this) + " "
                + "?:";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + " "
                + expr.right.accept(this) + " "
                + expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this) + " group";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return (expr.value == null)
                ? " null"
                : expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme + " var";
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return stmt.expression.accept(this) + " "
                + "print";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return stmt.initializer.accept(this) + " "
                + stmt.name.lexeme + " "
                + "var";
    }
}
