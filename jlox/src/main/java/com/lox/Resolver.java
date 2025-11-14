package com.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Map;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private enum FunctionType {
        NONE, FUNCTION
    }
    private class VarData {
        public Token declaration = null;
        public boolean used = false;
        public boolean initialized = false;

        VarData(Token declaration) { this.declaration = declaration; }
    }
    
    private final Interpreter interpreter;
    private final Stack<Map<String, VarData>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, VarData>());
    }

    private void endScope() {
        for (Map.Entry<String, VarData> entry : scopes.peek().entrySet()) {
            if (entry.getValue().used == Boolean.FALSE) {
                Lox.error(entry.getValue().declaration, "The value of '" + entry.getKey() + "' is never used");
            }
        }
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.empty()) return;
        Map<String, VarData> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope");
        }
        scope.put(name.lexeme, new VarData(name));
    }

    private void define(Token name) {
        if (scopes.empty()) return;
        scopes.peek().get(name.lexeme).initialized = true;
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                scopes.get(i).get(name.lexeme).used = true;
                return;
            }
        }
    }

    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;    
    }


    @Override
    public Void visitExpressionStmStatement(Statement.ExpressionStm statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        declare(statement.name);
        if (statement.initializer != null) {
            resolve(statement.initializer);
        }
        define(statement.name);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.thenBranch);
        if (statement.elseBranch != null) resolve(statement.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name);
        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(statement.keyword, "Can't return from top-level code.");
        }
        
        if (statement.value != null) resolve(statement.value);
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.value);
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitTernaryExpression(Expression.Ternary expression) {
        resolve(expression.left);
        resolve(expression.middle);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() && scopes.peek().containsKey(expression.name.lexeme) && 
            scopes.peek().get(expression.name.lexeme).initialized == Boolean.FALSE) {
            Lox.error(expression.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);

        for (Expression argument : expression.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitFunctionExpression(Expression.Function expression) {
        resolveFunction(expression.function, FunctionType.FUNCTION);
        return null;
    }
}
