package com.lox;

public class Return extends RuntimeException {

    /**
     * Store the return value of the function call.
     */
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
