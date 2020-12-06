package com.lox.ast;

import com.lox.Evaluator;
import com.lox.Token;

import java.util.List;

public class AstRPNPrinter implements Evaluator, Expr.Visitor<String>, Stmt.Visitor<String> {

    @Override
    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) {
            System.out.println(statement.accept(this));
        }
    }

    @Override
    public void resolve(Expr expr, int distance, int slot) {}

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
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder();
        for (Expr argument : expr.arguments) {
            builder.append(argument.accept(this));
            builder.append(" ");
        }
        builder.append(expr.callee.accept(this));
        builder.append(" call");
        return builder.toString();
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return expr.name.lexeme + " "
                + expr.object.accept(this) + " "
                + "get";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + " "
                + expr.right.accept(this) + " "
                + expr.operator.lexeme;
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        StringBuilder builder = new StringBuilder();
        buildFunctionBody(builder, expr);
        builder.append("fun");
        return builder.toString();
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
    public String visitSetExpr(Expr.Set expr) {
        return expr.name.lexeme + " "
                + expr.value.accept(this) + " "
                + expr.object.accept(this) + " "
                + "set";
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return expr.method.lexeme + " "
                + expr.keyword.lexeme + " "
                + "call";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return expr.keyword.lexeme;
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
    public String visitBreakStmt(Stmt.Break stmt) {
        return " break";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return stmt.body.accept(this)
                + stmt.condition.accept(this)
                + " while";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        StringBuilder builder = new StringBuilder();
        if (stmt.elseBranch != null) {
            builder.append(stmt.elseBranch.accept(this));
            builder.append(" ");
        }
        builder.append(stmt.thenBranch.accept(this))
                .append(" ")
                .append(stmt.condition.accept(this))
                .append(" if");
        return builder.toString();
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        // blockBegin and blockEnd are used as delimiter for a block:
        // the machine should add a scope in the environment.
        builder.append("( ");
        for (Stmt statement : stmt.statements) {
            builder.append(" ");
            builder.append(statement.accept(this));
        }
        builder.append(" )");
        return builder.toString();
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        return stmt.name + " class";
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        buildFunctionBody(builder, stmt.function);
        builder.append(" fun");
        return builder.toString();
    }

    private void buildFunctionBody(StringBuilder builder, Expr.Function function) {
        builder.append("( ");
        for (Stmt body : function.body) {
            builder.append(body.accept(this));
        }
        builder.append(") ");
        for (Token param : function.params) {
            if (param != function.params.get(0)) builder.append(" ");
            builder.append(param.lexeme);
        }
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return stmt.expression.accept(this) + " "
                + "print";
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return stmt.value.accept(this) + " "
                + "return";
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
            builder.append(" ");
            builder.append(stmt.name.lexeme);
            builder.append(" =");
        }
        return builder.toString();
    }
}
