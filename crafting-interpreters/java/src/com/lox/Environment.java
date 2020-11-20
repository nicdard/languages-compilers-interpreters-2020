package com.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Defines a new name (variable) in the current environment.
     * Definition of a new name can happen only in the current scope.
     * @param name
     * @param value
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Lookups in the environment chain to and returns the value for name.
     * @param name
     * @throws RuntimeError if name is not found in any scope.
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

    /**
     * Assigns value to an existing name.
     * @param name
     * @param value
     * @throws RuntimeError if name is not already defined.
     */
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme,value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
