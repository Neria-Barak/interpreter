package com.lox;

class AstPrinter implements Expression.Visitor<String> {
    String print(Expression expr) {
        return expr.accept(this);
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
}
