package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs a post-order traversal to evaluate the AST.
 */
public class Interpreter implements Evaluator, Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private static class BreakException extends RuntimeException {
        BreakException() { super(null, null, false, false); }
    }

    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (BreakException e) {
            // Do nothing.
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> stmts, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTruthy(evaluate(expr.guard))) {
            return evaluate(expr.then);
        } else {
            return evaluate(expr.elseBranch);
        }
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Call only function and classes.");
        }
        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                function.arity() + " arguments but got" +
                arguments.size() + "."
            );
        }
        return function.call(this, arguments);
    }

    /**
     * Evaluates a binary operator: first we evaluate both left and right
     * subexpressions, then we perform the operation.
     * @param expr
     */
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case COMMA:
                // Discard first operand result.
                return right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double)left + (double)right;
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                if (hasAStringOperand(left, right))
                    return stringify(left) + stringify(right);
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                checkNotZero(expr.operator, (double)right);
                return (double)left / (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(value);
            case MINUS:
                checkNumberOperand(expr.operator, expr.right);
                return -(double)value;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private Void execute(Stmt stmt) {
        return stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Ruby's truthiness rule: false and nil are falsey and everything else is truthy.
     * @param object any Lox's value.
     */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    /**
     * Checks the <code>item1</code> and <code>item2</code> are equals.
     */
    private boolean isEqual(Object item1, Object item2) {
        if (item1 == null && item2 == null) return true;
        if (item1 == null) return false;
        return item1.equals(item2);
    }

    /**
     * Checks that either <code>op1</code> and <code>op2</code>
     * is a string, throws a {@link RuntimeError} otherwise.
     */
    private boolean hasAStringOperand(Object ...ops) {
        for (Object op : ops) {
            if (op instanceof String) return true;
        }
        return false;
    }

    /**
     * Checks that <code>operand</code> is zero without casting it to an int.
     * @param operand
     */
    private void checkNotZero(Token operator, double operand) {
        if (operand != 0.0) return;
        throw new RuntimeError(operator, "Invalid 0 operand.");
    }

    /**
     * Checks that <code>operand</code> is a number, throws a {@link RuntimeError}
     * otherwise, signaling the operator which was supposed to be applied.
     * @param operator the unary operator.
     * @param operand the value on which the operator should be applied.
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Checks that <code>left</code> and <code>right</code> are numbers,
     * throws a {@link RuntimeError} otherwise, signaling the operator
     * which was supposed to be applied.
     * @param operator the binary operator.
     * @param left left-side of the binary expression.
     * @param right right-side of the binary expression.
     */
    private void checkNumberOperands(
        Token operator,
        Object left,
        Object right
    ) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * @return a string representation of <code>object</code>
     */
    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}
