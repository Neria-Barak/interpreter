package com.lox;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expression expression() {
        return commaOp();
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        } 
        return statements;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (RuntimeError err) {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token varName = consume(TokenType.IDENTIFIER, "Expected variable name.");

        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expectedd ';' after variable declaration.");
        return new Statement.Var(varName, initializer);
    }

    private Statement statement() {
        if (match(TokenType.LEFT_BRACE)) return new Statement.Block(block());
        if (match(TokenType.PRINT)) return printStatement();
        
        return expressionStatement();
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new Statement.Print(value);
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Statement.ExpressionStm(expression);
    }

    private Expression commaOp() {
        Expression expression = assignment();

        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expression right = ternary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression assignment() {
        Expression expression = ternary();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable)expression).name;
                return new Expression.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression ternary() {
        Expression expression = equality();

        if (match(TokenType.QUESTION)) {
            Token operator1 = previous();
            Expression middle = commaOp();

            consume(TokenType.COLON, "Expected ':' after then-branch of conditional");
            Token operator2 = previous();
            Expression right = ternary();
            return new Expression.Ternary(expression, operator1, middle, operator2, right);
        }

        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }
        return primary();
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) return new Expression.Literal(false);
        if (match(TokenType.TRUE)) return new Expression.Literal(true);
        if (match(TokenType.NIL)) return new Expression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expression expression = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression); 
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

        throw error(peek(), "Expected expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
            }

            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

}
