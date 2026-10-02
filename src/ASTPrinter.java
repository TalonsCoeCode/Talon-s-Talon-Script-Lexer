public class ASTPrinter {

    public static String print(AST ast) {
        if (ast == null) return "<no AST>";
        return new ASTPrinter().visit(ast);
    }

    private String visit(AST ast) {
        if (ast instanceof AST.Binary binary) {
            return "(" + visit(binary.left) + " "
                    + binary.operator.lexeme + " "
                    + visit(binary.right) + ")";
        }

        if (ast instanceof AST.Unary unary) {
            return "(" + unary.operator.lexeme + " "
                    + visit(unary.right) + ")";
        }

        if (ast instanceof AST.Grouping grouping) {
            // Binary nodes already use parentheses, so the grouping node
            // does not need to add a second pair when displayed.
            return visit(grouping.expression);
        }

        if (ast instanceof AST.Literal literal) {
            if (literal.value == null) return "none";
            if (literal.value instanceof String) return "\"" + literal.value + "\"";
            if (literal.value instanceof Double number && number == Math.rint(number)) {
                return Long.toString(number.longValue());
            }
            return literal.value.toString();
        }

        return "<unknown AST>";
    }
}