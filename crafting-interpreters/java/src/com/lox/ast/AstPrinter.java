package com.lox.ast;

import com.lox.Evaluator;
import com.lox.Token;

import java.util.List;

public class AstPrinter implements Evaluator, Expr.Visitor<String>, Stmt.Visitor<String> {

    @Override
    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) {
            System.out.println(statement.accept(this));
        }
    }

    @Override
    public void resolve(Expr expr, int distance, int slot) {
        // TODO add maps of locals and print the whole information after the AST.
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize("?:",expr.guard, expr.then, expr.elseBranch);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize2("call", expr.callee, expr.arguments);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return parenthesize2("get", expr.name, expr.object);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("(fun ").append("(");
        buildsFunctionBody(builder, expr);
        builder.append(")");
        return builder.toString();
    }

    private void buildsFunctionBody(StringBuilder builder, Expr.Function expr) {
        for (Token param : expr.params) {
            if (param != expr.params.get(0)) builder.append(" ");
            builder.append(param.lexeme);
        }
        builder.append(") ");
        for (Stmt body : expr.body) {
            builder.append(body.accept(this));
        }
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return (expr.value == null)
                ? "nil"
                : expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return parenthesize2("set", expr.name, expr.object, expr.value);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return parenthesize2("super", expr.method);
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return expr.keyword.lexeme;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "break";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return parenthesize2("while", stmt.condition, stmt.body);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if (stmt.elseBranch != null) {
            return parenthesize2("if", stmt.condition, stmt.thenBranch, stmt.elseBranch);
        } else {
            return parenthesize2("if", stmt.condition, stmt.thenBranch);
        }
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block");
        for (Stmt statement : stmt.statements) {
            builder.append(" ");
            builder.append(statement.accept(this));
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        return parenthesize2("class", stmt.name);
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize(";", stmt.expression);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(fun ").append(stmt.name.lexeme).append("(");
        buildsFunctionBody(builder, stmt.function);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return parenthesize("return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return parenthesize2("var", stmt.name);
        }
        return parenthesize2("var",stmt.name, "=", stmt.initializer);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize2("=", expr.name.lexeme, expr.value);
    }

    private String parenthesize(String name, Expr ...exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append('(').append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    private String parenthesize2(String name, Object ...parts) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        transform(builder, parts);
        builder.append(")");
        return builder.toString();
    }

    private void transform(StringBuilder builder, Object... parts) {
        for (Object part : parts) {
            builder.append(" ");
            if (part instanceof Expr) {
                builder.append(((Expr)part).accept(this));
            } else if (part instanceof Stmt) {
                builder.append(((Stmt) part).accept(this));
            } else if (part instanceof Token) {
                builder.append(((Token) part).lexeme);
            } else if (part instanceof List) {
                transform(builder, ((List) part).toArray());
            } else {
                builder.append(part);
            }
        }
    }
}
