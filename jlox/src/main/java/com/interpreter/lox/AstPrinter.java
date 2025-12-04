package com.interpreter.lox;

import java.util.List;

class AstPrinter implements Expression.Visitor<String>, Statement.Visitor<String> {
    String print(Expression expr) {
        return expr.accept(this);
    }

    String print(Statement stmt) {
        return stmt.accept(this);
    }

    String print(List<Statement> statements) {
        StringBuilder builder = new StringBuilder();
        for (Statement stmt : statements) {
            builder.append(print(stmt));
            builder.append(";\n");
        }
        return builder.toString();
    }

    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    } 


    // expressions
    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitTernaryExpression(Expression.Ternary expression) {
        return parenthesize(expression.operator1.lexeme + expression.operator2.lexeme, 
                            expression.left, expression.middle, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        return expression.value == null ? "nil" : expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return expression.name.lexeme;
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return parenthesize("= " + expression.name.lexeme, expression.value);
    }

    
    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitCallExpression(Expression.Call expression) {
        StringBuilder builder = new StringBuilder(print(expression.callee));
        builder.append("(");
        int i;
        for (i = 0; i < expression.arguments.size() - 1; i++) {
            builder.append(print(expression.arguments.get(i)));
            builder.append(", ");
        }
        if (expression.arguments.size() > 0) builder.append(print(expression.arguments.get(i)));
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitFunctionExpression(Expression.Function expression) {
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionExpression'");
    }
    
    @Override
    public String visitGetExpression(Expression.Get expression) {
        throw new UnsupportedOperationException("Unimplemented method 'visitGetExpression'");
    }
    
    @Override
    public String visitSetExpression(Expression.Set expression) {
        throw new UnsupportedOperationException("Unimplemented method 'visitSetExpression'");
    }
    
    @Override
    public String visitThisExpression(Expression.This expression) {
        throw new UnsupportedOperationException("Unimplemented method 'visitThisExpression'");
    }
    
    @Override
    public String visitSuperExpression(Expression.Super expression) {
        throw new UnsupportedOperationException("Unimplemented method 'visitSuperExpression'");
    }
    
    @Override
    public String visitArrayExpression(Expression.Array expression) {
        StringBuilder builder = new StringBuilder("[");
        int i;
        for (i = 0; i < expression.elements.size() - 1; i++) {
            builder.append(print(expression.elements.get(i)));
            builder.append(", ");
        }
        if (expression.elements.size() > 0) builder.append(print(expression.elements.get(i)));
        builder.append("]");

        return builder.toString();
    }
    
    @Override
    public String visitSubscriptionExpression(Expression.Subscription expression) {
        StringBuilder builder = new StringBuilder(print(expression.arr));
        builder.append("[");
        builder.append(print(expression.index));
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    public String visitArrayAssExpression(Expression.ArrayAss expression) {
        StringBuilder builder = new StringBuilder(print(expression.array));
        builder.append("[");
        builder.append(print(expression.index));
        builder.append("] = ");
        builder.append(print(expression.value));
        return builder.toString();    
    }

    // statements
    @Override
    public String visitExpressionStmStatement(Statement.ExpressionStm statement) {
        return print(statement.expression);
    }

    @Override
    public String visitPrintStatement(Statement.Print statement) {
        return "print(" + print(statement.expression) + ")";
    }

    @Override
    public String visitVarStatement(Statement.Var statement) {
        if (statement.initializer == null) return "var " + statement.name.lexeme; 
        return "var " + statement.name.lexeme + " = " + print(statement.initializer);
    }

    @Override
    public String visitBlockStatement(Statement.Block statement) {
        return "{\n" + print(statement.statements) + "}\n";
    }

    @Override
    public String visitIfStatement(Statement.If statement) {
        String condition =  "if (" + print(statement.condition) + ")";
        String  thenBranch = " {\n" + print(statement.thenBranch) + "\n}";
        String stmt = condition + thenBranch;
        if (statement.elseBranch != null) {
            stmt += " else {\n" + print(statement.elseBranch) + "\n}";
        }
        return stmt;
    }

    @Override
    public String visitWhileStatement(Statement.While statement) {
        String condition =  "while (" + print(statement.condition) + ")";
        String  body = " {\n" + print(statement.body) + "\n}";
        return condition + body;    
    }

    @Override
    public String visitBreakStatement(Statement.Break statement) {
        return "break;\n";
    }

    @Override
    public String visitFunctionStatement(Statement.Function statement) {
        StringBuilder ret = new StringBuilder("fun " + statement.name.lexeme + "(");
        
        for (Token param : statement.params) {
            ret.append(param.lexeme + ", ");
        }
        ret.append(") {\n");
        ret.append(print(statement.body));
        ret.append("\n}");
        return ret.toString();
    }

    @Override
    public String visitReturnStatement(Statement.Return statement) {
        if (statement.value == null) return "return;\n";
        return "return " + print(statement.value) + ";\n";
    }

    @Override
    public String visitClassStatement(Statement.Class statement) {
        throw new UnsupportedOperationException("Unimplemented method 'visitClassStatement'");
    }
}