package com.lox;

import com.lox.ast.Expr;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(String name, Expr.Function declaration, Environment closure, boolean isInitializer) {
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    /**
     * {@link LoxFunction#bind}: When the function is an initializer the closure is
     * for sure the binding environment, thus we know for sure that the first and only
     * element in the closure is a reference to the {@link LoxInstance}.
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < arity(); ++i) {
            environment.define(arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, 0);
            return returnValue.value;
        }
        if (isInitializer) return closure.getAt(0, 0);
        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define(instance);
        return new LoxFunction(name, declaration, environment, isInitializer);
    }

    @Override
    public String toString() {
        return name == null
                ? "<fn>"
                : "<fn " + name + ">";
    }
}
