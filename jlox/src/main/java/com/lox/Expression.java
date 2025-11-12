package com.lox;

abstract class Expression {
	interface Visitor<T> {
		T visitAssignExpression(Assign expression);
		T visitBinaryExpression(Binary expression);
		T visitTernaryExpression(Ternary expression);
		T visitGroupingExpression(Grouping expression);
		T visitLiteralExpression(Literal expression);
		T visitUnaryExpression(Unary expression);
		T visitVariableExpression(Variable expression);
	}

	static class Assign extends Expression {
		final Token name;
		final Expression value;

		Assign(Token name, Expression value) {
			this.name = name;
			this.value = value;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitAssignExpression(this);
		}
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
	static class Ternary extends Expression {
		final Expression left;
		final Token operator1;
		final Expression middle;
		final Token operator2;
		final Expression right;

		Ternary(Expression left, Token operator1, Expression middle, Token operator2, Expression right) {
			this.left = left;
			this.operator1 = operator1;
			this.middle = middle;
			this.operator2 = operator2;
			this.right = right;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitTernaryExpression(this);
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
	static class Variable extends Expression {
		final Token name;

		Variable(Token name) {
			this.name = name;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitVariableExpression(this);
		}
	}

	abstract <T> T accept(Visitor<T> visitor);
}
