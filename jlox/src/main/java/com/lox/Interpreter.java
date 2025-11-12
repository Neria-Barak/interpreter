package com.lox;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment environment = new Environment();
    
    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError err) {
            Lox.runtimeError(err);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private String stringify(Object obj) {
        if (obj == null) return "nil";
        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return obj.toString();
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    // Interpret expressions:
    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch(expression.operator.type) {
            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                return (double)left - (double)right;
            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                if ((double)right == 0) throw new RuntimeError(expression.operator, "Division by zero detected.");
                return (double)left / (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                if (left instanceof String) {
                    return (String)left + stringify(right);
                }
                if (right instanceof String) {
                    return stringify(left) + (String) right;
                }
                throw new RuntimeError(expression.operator, "Operands must be two numbers or two strings.");

            case LESS:
                checkNumberOperands(expression.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double)left <= (double)right;
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double)left >= (double)right;

            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);

            case COMMA:
                return right;
                
            default:
        }

        return null;
    }

    @Override
    public Object visitTernaryExpression(Expression.Ternary expression) {
        Object left = evaluate(expression.left);
        Object middle = evaluate(expression.middle);
        Object right = evaluate(expression.right);
        
        return isTrue(left) ? middle : right;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right);
        switch (expression.operator.type) {
            case MINUS:
                checkNumberOperand(expression.operator, right);
                return -(double)right;
            case BANG:
                return !isTrue(right);
            default:
        }
        return null;
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.value);
        environment.assign(expression.name, value);
        return value;
    }

    public Object visitVariableExpression(Expression.Variable expression) {
        return environment.get(expression.name);
    }

    private boolean isTrue(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (boolean)obj;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a numbers");
    }

    // Interpret statements:
    @Override
    public Void visitExpressionStmStatement(Statement.ExpressionStm statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }
        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}
