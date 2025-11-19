package com.interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private boolean isBreaking = false;
    private final Map<Expression, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }
    
    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    private Object lookupVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

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
        if (obj instanceof List<?>) {
            StringBuilder ret = new StringBuilder("[");
            List<?> arr = (List<?>)obj;
            int i;
            for (i = 0; i < arr.size() - 1; i++) {
                ret.append(stringify(arr.get(i)));
                ret.append(", ");
            }
            if (arr.size() > 0) ret.append(stringify(arr.get(i)));
            ret.append("]");
            return ret.toString();
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
                return (Double)left - (Double)right;
            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (Double)left * (Double)right;
            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                if ((Double)right == 0) throw new RuntimeError(expression.operator, "Division by zero detected.");
                return (Double)left / (Double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double)left + (Double)right;
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
                return (Double)left < (Double)right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (Double)left <= (Double)right;
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (Double)left > (Double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (Double)left >= (Double)right;

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
        
        return isTrue(left) ? evaluate(expression.middle) : evaluate(expression.right);
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
                return -(Double)right;
            case BANG:
                return !isTrue(right);
            default:
        }
        return null;
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.value);

        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name, value);
        } else {
            globals.assign(expression.name, value);
        }

        return value;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        // return environment.get(expression.name);
        return lookupVariable(expression.name, expression);
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = evaluate(expression.left);

        if (expression.operator.type == TokenType.OR) {
            if (isTrue(left)) return left;
        } else {
            if (!isTrue(left)) return left;
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitCallExpression(Expression.Call expression) {
        Object callee = evaluate(expression.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expression.paren, "Can only call function and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitFunctionExpression(Expression.Function statement) {
        LoxFunction function = new LoxFunction(statement.function, environment, false);
        return function;
    }

    @Override
    public Object visitGetExpression(Expression.Get expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance)object).get(expression.name, this);
        }

        throw new RuntimeError(expression.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpression(Expression.Set expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance) {
            Object value = evaluate(expression.value);
            ((LoxInstance)object).set(expression.name, value);
            return value;
        }

        throw new RuntimeError(expression.name, "Only instances have fields.");
    }

    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookupVariable(expression.keyword, expression);
    }

    @Override
    public Object visitSuperExpression(Expression.Super expression) {
        int distance = locals.get(expression);
        LoxClass superclass = (LoxClass)environment.getAt(distance, "super");
        LoxInstance object = (LoxInstance)environment.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expression.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expression.method, "Undefined property '" + expression.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitArrayExpression(Expression.Array expression) {
        List<Object> arr = new ArrayList<>();
        for (Expression expr : expression.elements) {
            arr.add(evaluate(expr));
        }
        return arr;
    }

    @Override
    public Object visitSubscriptionExpression(Expression.Subscription expression) {
        Object arr = evaluate(expression.arr);
        if (!(arr instanceof List<?>)) {
            throw new RuntimeError(expression.bracket, "Can only access index of arrays.");
        }
        List<?> list = (List<?>)arr;
        Object index = evaluate(expression.index);
        if (!(index instanceof Double) || ((Double)index % 1) != 0) {
            throw new RuntimeError(expression.bracket, "Index must be integer.");
        }
        
        int intIndex = ((Double)index).intValue();
        if (intIndex > list.size()) {
            throw new RuntimeError(expression.bracket, "Index out of bounds.");
        }
        return list.get(intIndex);
    }

    @Override
    public Object visitArrayAssExpression(Expression.ArrayAss expression) {
        Object array = evaluate(expression.array);
        Object index = evaluate(expression.index);
        Object value = evaluate(expression.value);
        
        if (!(array instanceof List<?>)) {
            throw new RuntimeError(expression.bracket, "Can only access index of arrays.");
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>)array;
        if (!(index instanceof Double) || ((Double)index % 1) != 0) {
            throw new RuntimeError(expression.bracket, "Index must be integer.");
        }

        int intIndex = ((Double)index).intValue();
        if (intIndex > list.size()) {
            throw new RuntimeError(expression.bracket, "Index out of bounds.");
        }
        return list.set(intIndex, value);
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

    @Override
    public Void visitIfStatement(Statement.If statement) {
        Object cond = evaluate(statement.condition);
        if (isTrue(cond)) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTrue(evaluate(statement.condition))) {
            execute(statement.body);
            if (isBreaking) break;
        }
        isBreaking = false;
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        isBreaking = true;

        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        LoxFunction function = new LoxFunction(statement, environment, false);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        Object superclass = null;
        if (statement.superclass != null) {
            superclass = evaluate(statement.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(statement.superclass.name, "Superclass must be a class.");
            }
        }
        
        environment.define(statement.name.lexeme, null);

        if (statement.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Statement.Function method : statement.methods) {
            LoxFunction function = new LoxFunction(method, environment, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        Map<String, LoxFunction> getters = new HashMap<>();
        for (Statement.Function getter : statement.getters) {
            LoxFunction function = new LoxFunction(getter, environment, false);
            getters.put(getter.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(statement.name.lexeme, (LoxClass)superclass, methods, getters);

        if (superclass != null) environment = environment.enclosing;

        environment.assign(statement.name, klass);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value != null) value = evaluate(statement.value);

        throw new Return(value);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
                if (isBreaking) break;
            }
        } finally {
            this.environment = previous;
        }
    }
}
