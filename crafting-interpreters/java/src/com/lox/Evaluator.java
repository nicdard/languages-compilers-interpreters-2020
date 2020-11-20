package com.lox;

import com.lox.ast.Stmt;

import java.util.List;

public interface Evaluator {

    void interpret(List<Stmt> statements);
}