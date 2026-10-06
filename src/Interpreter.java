public class Interpreter {

    public Object interpret(AST ast) {
        try {
            return evaluate(ast);
        } catch (RuntimeError error) {
            TalonScript.raiseError("Runtime error: " + error.getMessage(), error.line);
            return null;
        }
    }

    private Object evaluate(AST ast) {
        if (ast instanceof AST.Literal literal) {
            return literal.value;
        }

        if (ast instanceof AST.Grouping grouping) {
            return evaluate(grouping.expression);
        }

        if (ast instanceof AST.Unary unary) {
            Object right = evaluate(unary.right);

            return switch (unary.operator.type) {
                case MINUS -> {
                    checkNumberOperand(unary.operator, right);
                    yield -((Double) right);
                }
                case NOT -> !isTruthy(right);
                default -> throw new RuntimeError(unary.operator,
                        "Unknown unary operator '" + unary.operator.lexeme + "'.");
            };
        }

        if (ast instanceof AST.Binary binary) {
            Object left = evaluate(binary.left);
            Object right = evaluate(binary.right);

            return evaluateBinary(binary.operator, left, right);
        }

        throw new RuntimeException("Unknown AST node.");
    }

    private Object evaluateBinary(Token operator, Object left, Object right) {
        switch (operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (Double) left + (Double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(operator,
                        "Operands of '+' must both be numbers or both be strings.");

            case MINUS:
                checkNumberOperands(operator, left, right);
                return (Double) left - (Double) right;

            case MULT:
                checkNumberOperands(operator, left, right);
                return (Double) left * (Double) right;

            case DIV:
                checkNumberOperands(operator, left, right);
                // Java double arithmetic gives Infinity/NaN for division by zero.
                // This is intentional: e.g. (5 / 0) == (8 / 0) evaluates to true.
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

    private boolean isTruthy(Object value) {
        // Language rule: false, none, and the empty string are falsey.
        // All other values, including 0 and nonempty strings, are truthy.
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return !((String) value).isEmpty();
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;

        // Equality is type-sensitive.  Therefore "3" == 3 is false.
        if (!left.getClass().equals(right.getClass())) return false;

        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;

        throw new RuntimeError(operator,
                "Operand must be a number, but got " + typeName(operand) + ".");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
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

    private static class RuntimeError extends RuntimeException {
        final int line;

        RuntimeError(Token token, String message) {
            super(message);
            this.line = token.line;
        }
    }
}

