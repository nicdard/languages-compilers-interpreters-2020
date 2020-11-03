package com.lox;

import com.lox.ast.Expr;

/**
 * Performs a post-order traversal to evaluate the AST.
 */
public class Interpreter implements Expr.Visitor<Object> {

    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return null;
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
            case BANG_EQUAL: return isEqual(left, right);
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
