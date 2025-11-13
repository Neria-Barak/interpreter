package com.lox;

import java.util.List;

abstract class Statement {
	interface Visitor<T> {
		T visitExpressionStmStatement(ExpressionStm statement);
		T visitPrintStatement(Print statement);
		T visitVarStatement(Var statement);
		T visitBlockStatement(Block statement);
		T visitIfStatement(If statement);
		T visitWhileStatement(While statement);
		T visitBreakStatement(Break statement);
		T visitFunctionStatement(Function statement);
		T visitReturnStatement(Return statement);
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
	static class If extends Statement {
		final Expression condition;
		final Statement thenBranch;
		final Statement elseBranch;

		If(Expression condition, Statement thenBranch, Statement elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitIfStatement(this);
		}
	}
	static class While extends Statement {
		final Expression condition;
		final Statement body;

		While(Expression condition, Statement body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitWhileStatement(this);
		}
	}
	static class Break extends Statement {
		final Token keyword;

		Break(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitBreakStatement(this);
		}
	}
	static class Function extends Statement {
		final Token name;
		final List<Token> params;
		final List<Statement> body;

		Function(Token name, List<Token> params, List<Statement> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitFunctionStatement(this);
		}
	}
	static class Return extends Statement {
		final Token keyword;
		final Expression value;

		Return(Token keyword, Expression value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		<T> T accept(Visitor<T> visitor) {
			return visitor.visitReturnStatement(this);
		}
	}

	abstract <T> T accept(Visitor<T> visitor);
}
