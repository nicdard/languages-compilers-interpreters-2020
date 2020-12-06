package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs a post-order traversal to evaluate the AST.
 */
public class Interpreter implements Evaluator, Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private static class BreakException extends RuntimeException {
        BreakException() { super(null, null, false, false); }
    }

    private final Map<String, Object> globals = new HashMap<>();
    private Environment environment;
    /** */
    private final Map<Expr, Integer> locals = new HashMap<>();
    /** */
    private final Map<Expr, Integer> slots = new HashMap<>();

    Interpreter() {
        globals.put("clock", new LoxCallable() {
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

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(
                        stmt.superclass.name,
                        "Superclass must be a class"
                );
            } else {
                environment = new Environment(environment);
                environment.define(superclass);
            }
        }
        Map<String, LoxFunction> classMethods = new HashMap<>();
        for (Stmt.Function classMethod : stmt.classMethods) {
            LoxFunction function = new LoxFunction(
                    null,
                    classMethod.function,
                    environment,
                    false
            );
            classMethods.put(classMethod.name.lexeme, function);
        }
        LoxClass metaclass = new LoxClass(null,
                stmt.name.lexeme + " metaclass", null, classMethods);
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(
                    null,
                    method.function,
                    environment,
                    method.name.lexeme.equals("init")
            );
            methods.put(method.name.lexeme, function);
        }
        LoxClass kclass = new LoxClass(metaclass, stmt.name.lexeme, (LoxClass)superclass, methods);
        if (superclass != null) {
            environment = environment.enclosing;
        }
        define(stmt.name, kclass);
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
        LoxFunction function = new LoxFunction(stmt.name.lexeme, stmt.function, environment, false);
        define(stmt.name, function);
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
        define(stmt.name, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, slots.get(expr), value);
        } else {
            globals.put(expr.name.lexeme, value);
        }
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

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties.");
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
    public Object visitFunctionExpr(Expr.Function expr) {
        return new LoxFunction(null, expr, environment, false);
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
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }
        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        int slot = slots.get(expr);
        LoxClass superclass = (LoxClass)environment.getAt(distance, slot);
        LoxInstance object = (LoxInstance)environment.getAt(distance - 1, 0);
        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
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
        return lookupVariable(expr.name, expr);
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

    /**
     * Stores information about the nesting of each expression
     * @param expr
     * @param depth
     */
    public void resolve(Expr expr, int depth, int slot) {
        locals.put(expr, depth);
        slots.put(expr, slot);
    }

    /**
     * @param name
     * @param expr
     * @return the variable value from the right environment.
     */
    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, slots.get(expr));
        } else {
            return globals.get(name.lexeme);
        }
    }

    private void define(Token name, Object value) {
        if (environment != null) {
            environment.define(value);
        } else {
            globals.put(name.lexeme, value);
        }
    }
}
