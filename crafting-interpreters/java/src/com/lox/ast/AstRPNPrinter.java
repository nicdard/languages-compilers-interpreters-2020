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
        return  expr.value.accept(this) + " "
                + expr.name.lexeme + " "
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
    public String visitLogicalExpr(Expr.Logical expr) {
        return expr.left.accept(this) + " "
                + expr.right.accept(this) + " "
                + expr.operator.lexeme;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        if (stmt.elseBranch != null) {
            builder.append(stmt.elseBranch);
        }
        builder.append(stmt.thenBranch);
        builder.append(stmt.condition);
        builder.append("if");
        return builder.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        // blockBegin and blockEnd are used as delimiter for a block:
        // the machine should add a scope in the environment.
        builder.append("blockBegin");
        for (Stmt statement : stmt.statements) {
            builder.append("\n");
            builder.append(statement.accept(this));
        }
        builder.append(" blockEnd");
        return builder.toString();
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
        StringBuilder builder = new StringBuilder();
        // We define the variable first, so the machine will find the definition.
        builder.append(stmt.name.lexeme);
        builder.append(" var");
        // Then we assign to the already defined variable the initialization value.
        if (stmt.initializer != null) {
            builder.append(" ");
            builder.append(stmt.initializer.accept(this));
            builder.append(" =");
        }
        return builder.toString();
    }
}
