package com.lox.ast;

public class AstRPNPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
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
}
