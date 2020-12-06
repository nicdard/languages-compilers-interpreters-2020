package com.lox.ast;

import com.lox.Token;
import java.util.List;

public abstract class Stmt {

	public interface Visitor<R> {
		R visitBreakStmt(Break stmt);
		R visitWhileStmt(While stmt);
		R visitIfStmt(If stmt);
		R visitBlockStmt(Block stmt);
		R visitClassStmt(Class stmt);
		R visitExpressionStmt(Expression stmt);
		R visitFunctionStmt(Function stmt);
		R visitPrintStmt(Print stmt);
		R visitReturnStmt(Return stmt);
		R visitVarStmt(Var stmt);
	}

	public static class Break extends Stmt {
		public Break(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}

		public final Token keyword;
	}

	public static class While extends Stmt {
		public While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		public final Expr condition;
		public final Stmt body;
	}

	public static class If extends Stmt {
		public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		public final Expr condition;
		public final Stmt thenBranch;
		public final Stmt elseBranch;
	}

	public static class Block extends Stmt {
		public Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		public final List<Stmt> statements;
	}

	public static class Class extends Stmt {
		public Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> classMethods) {
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
			this.classMethods = classMethods;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}

		public final Token name;
		public final Expr.Variable superclass;
		public final List<Stmt.Function> methods;
		public final List<Stmt.Function> classMethods;
	}

	public static class Expression extends Stmt {
		public Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		public final Expr expression;
	}

	public static class Function extends Stmt {
		public Function(Token name, Expr.Function function) {
			this.name = name;
			this.function = function;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}

		public final Token name;
		public final Expr.Function function;
	}

	public static class Print extends Stmt {
		public Print(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		public final Expr expression;
	}

	public static class Return extends Stmt {
		public Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

		public final Token keyword;
		public final Expr value;
	}

	public static class Var extends Stmt {
		public Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		public final Token name;
		public final Expr initializer;
	}

	public abstract <R> R accept(Visitor<R> visitor);
}
