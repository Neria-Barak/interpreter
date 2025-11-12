package com.lox;

import java.util.List;

abstract class Statement {
	interface Visitor<T> {
		T visitExpressionStmStatement(ExpressionStm statement);
		T visitPrintStatement(Print statement);
		T visitVarStatement(Var statement);
		T visitBlockStatement(Block statement);
	}

	static class ExpressionStm extends Statement {
		final Expression expression;

		ExpressionStm(Expression expression) {
			this.expression = expression;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitExpressionStmStatement(this);
		}
	}
	static class Print extends Statement {
		final Expression expression;

		Print(Expression expression) {
			this.expression = expression;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitPrintStatement(this);
		}
	}
	static class Var extends Statement {
		final Token name;
		final Expression initializer;

		Var(Token name, Expression initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitVarStatement(this);
		}
	}
	static class Block extends Statement {
		final List<Statement> statements;

		Block(List<Statement> statements) {
			this.statements = statements;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitBlockStatement(this);
		}
	}

	abstract <T> T accept(Visitor<T> visitor);
}
