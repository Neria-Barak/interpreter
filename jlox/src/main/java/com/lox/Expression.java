package com.lox;

abstract class Expression {
	interface Visitor<T> {
		T visitBinaryExpression(Binary expression);
		T visitGroupingExpression(Grouping expression);
		T visitLiteralExpression(Literal expression);
		T visitUnaryExpression(Unary expression);
	}

	static class Binary extends Expression {
		final Expression left;
		final Token operator;
		final Expression right;

		Binary(Expression left, Token operator, Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitBinaryExpression(this);
		}
	}
	static class Grouping extends Expression {
		final Expression expression;

		Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitGroupingExpression(this);
		}
	}
	static class Literal extends Expression {
		final Object value;

		Literal(Object value) {
			this.value = value;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitLiteralExpression(this);
		}
	}
	static class Unary extends Expression {
		final Token operator;
		final Expression right;

		Unary(Token operator, Expression right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitUnaryExpression(this);
		}
	}

	abstract <T> T accept(Visitor<T> visitor);
}
