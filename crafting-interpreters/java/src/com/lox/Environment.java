package com.lox;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    final Environment enclosing;
    private final List<Object> values = new ArrayList<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Defines a new name (variable) in the current environment.
     * Definition of a new name can happen only in the current scope.
     * @param value
     */
    void define(Object value) {
        values.add(value);
    }

    /**
     * Returns the value of name in the ancestor environment
     * distant distance from this.
     * @param distance
     * @param slot
     */
    Object getAt(int distance, int slot) {
        return ancestor(distance).values.get(slot);
    }

    void assignAt(int distance, int slot, Object value) {
        ancestor(distance).values.set(slot, value);
    }

    /**
     * @param distance
     * @return the ancestor environment distant distance from this.
     */
    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = distance - 1; i >= 0; --i) {
            environment = environment.enclosing;
        }
        return environment;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(values.toString());
        if (enclosing != null) {
            result.append(" -> ").append(enclosing.toString());
        }
        return result.toString();
    }
}
