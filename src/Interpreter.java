
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter {

    private Environment environment = new Environment();

    public Object interpret(AST ast) {
        try {
            return execute(ast);
        } catch (RuntimeError error) {
            TalonScript.raiseError(
                    "Runtime error: " + error.getMessage(),
                    error.line
            );
            return null;
        }
    }

    // Executes statements and evaluates expressions.
    private Object execute(AST ast) {

        if (ast instanceof AST.Program program) {
            for (AST statement : program.statements) {
                execute(statement);
            }
            return null;
        }

        if (ast instanceof AST.Literal literal) {
            return literal.value;
        }

        if (ast instanceof AST.Grouping grouping) {
            return evaluate(grouping.expression);
        }

        if (ast instanceof AST.Variable variable) {
            return environment.get(variable.name);
        }

        if (ast instanceof AST.Assign assign) {
            Object value = evaluate(assign.value);
            environment.assign(assign.name, value);
            return value;
        }

        if (ast instanceof AST.Unary unary) {
            Object right = evaluate(unary.right);

            return switch (unary.operator.type) {
                case MINUS -> {
                    checkNumberOperand(unary.operator, right);
                    yield -((Double) right);
                }

                case NOT -> !isTruthy(right);

                default -> throw new RuntimeError(
                        unary.operator,
                        "Unknown unary operator."
                );
            };
        }

        if (ast instanceof AST.Binary binary) {
            Object left = evaluate(binary.left);
            Object right = evaluate(binary.right);

            return evaluateBinary(binary.operator, left, right);
        }

        // Logical operations use short-circuit evaluation.
        if (ast instanceof AST.Logical logical) {
            Object left = evaluate(logical.left);

            if (logical.operator.type == TokenType.OR) {
                if (isTruthy(left)) {
                    return left;
                }
            } else {
                if (!isTruthy(left)) {
                    return left;
                }
            }

            return evaluate(logical.right);
        }

        // Variable declaration.
        if (ast instanceof AST.Var var) {
            Object value = null;

            if (var.initializer != null) {
                value = evaluate(var.initializer);
            }

            environment.define(var.name.lexeme, value);
            return null;
        }

        // Write statement.
        if (ast instanceof AST.Print print) {
            Object value = evaluate(print.expression);

            System.out.println(
                    value == null ? "none" : value
            );

            return null;
        }

        // Expression statement.
        if (ast instanceof AST.Expression expression) {
            evaluate(expression.expression);
            return null;
        }

        // Block statement.
        if (ast instanceof AST.Block block) {
            executeBlock(block.statements, new Environment(environment));
            return null;
        }

        // If statement.
        if (ast instanceof AST.If ifStatement) {
            if (isTruthy(evaluate(ifStatement.condition))) {
                execute(ifStatement.thenBranch);
            } else if (ifStatement.elseBranch != null) {
                execute(ifStatement.elseBranch);
            }

            return null;
        }

        // While loop.
        if (ast instanceof AST.While whileStatement) {
            while (isTruthy(evaluate(whileStatement.condition))) {
                execute(whileStatement.body);
            }

            return null;
        }

        throw new RuntimeException("Unknown AST node.");
    }

    private Object evaluate(AST ast) {
        return execute(ast);
    }

    // Executes statements inside a new scope.
    private void executeBlock(
            List<AST> statements,
            Environment newEnvironment
    ) {
        Environment previous = environment;

        try {
            environment = newEnvironment;

            for (AST statement : statements) {
                execute(statement);
            }
        } finally {
            environment = previous;
        }
    }

    // Binary operations.
    private Object evaluateBinary(
            Token operator,
            Object left,
            Object right
    ) {
        switch (operator.type) {

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left + (Double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(operator,
                        "Operands of '+' must both be numbers or strings.");

            case MINUS:
                checkNumberOperands(operator, left, right);
                return (Double) left - (Double) right;

            case MULT:
                checkNumberOperands(operator, left, right);
                return (Double) left * (Double) right;

            case DIV:
                checkNumberOperands(operator, left, right);
                return (Double) left / (Double) right;

            case GREATER:
                checkNumberOperands(operator, left, right);
                return (Double) left > (Double) right;

            case GREATER_EQUAL:
                checkNumberOperands(operator, left, right);
                return (Double) left >= (Double) right;

            case LESS:
                checkNumberOperands(operator, left, right);
                return (Double) left < (Double) right;

            case LESS_EQUAL:
                checkNumberOperands(operator, left, right);
                return (Double) left <= (Double) right;

            case EQUAL:
                return isEqual(left, right);

            case NOT_EQUAL:
                return !isEqual(left, right);

            default:
                throw new RuntimeError(operator,
                        "Unknown binary operator '" + operator.lexeme + "'.");
        }
    }

    // Truthiness rules.
    private boolean isTruthy(Object value) {
        if (value == null) return false;

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            return !((String) value).isEmpty();
        }

        return true;
    }

    // Equality.
    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;

        if (!left.getClass().equals(right.getClass())) {
            return false;
        }

        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;

        throw new RuntimeError(operator,
                "Operand must be a number, but got " + typeName(operand) + ".");
    }

    private void checkNumberOperands(
            Token operator,
            Object left,
            Object right
    ) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator,
                "Operands must both be numbers, but got "
                        + typeName(left) + " and " + typeName(right) + ".");
    }

    private String typeName(Object value) {
        if (value == null) return "none";
        if (value instanceof Double) return "number";
        if (value instanceof String) return "string";
        if (value instanceof Boolean) return "boolean";

        return value.getClass().getSimpleName();
    }

    // Runtime error.
    private static class RuntimeError extends RuntimeException {
        final int line;

        RuntimeError(Token token, String message) {
            super(message);
            this.line = token.line;
        }
    }

    // Variable environment and nested scopes.
    private static class Environment {
        private final Map<String, Object> values = new HashMap<>();
        private final Environment enclosing;

        Environment() {
            enclosing = null;
        }

        Environment(Environment enclosing) {
            this.enclosing = enclosing;
        }

        void define(String name, Object value) {
            values.put(name, value);
        }

        Object get(Token name) {
            if (values.containsKey(name.lexeme)) {
                return values.get(name.lexeme);
            }

            if (enclosing != null) {
                return enclosing.get(name);
            }

            throw new RuntimeError(
                    name,
                    "Undefined variable '" + name.lexeme + "'."
            );
        }

        void assign(Token name, Object value) {
            if (values.containsKey(name.lexeme)) {
                values.put(name.lexeme, value);
                return;
            }

            if (enclosing != null) {
                enclosing.assign(name, value);
                return;
            }

            throw new RuntimeError(
                    name,
                    "Undefined variable '" + name.lexeme + "'."
            );
        }
    }
}

