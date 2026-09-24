public class TestAST {
    public static void main(String[] args) {
        Token plus = new Token("+", null, 1, TokenType.PLUS);

        AST left = new AST.Literal(5.0);
        AST right = new AST.Literal(10.0);
        AST tree = new AST.Binary(left, plus, right);

        System.out.println("AST created successfully.");
        System.out.println("Root: " + tree.getClass().getSimpleName());

        AST.Binary binary = (AST.Binary) tree;
        System.out.println("Left: " + ((AST.Literal) binary.left).value);
        System.out.println("Operator: " + binary.operator.lexeme);
        System.out.println("Right: " + ((AST.Literal) binary.right).value);

        AST.Unary unary = new AST.Unary(plus, new AST.Literal(5.0));
        AST.Grouping grouping = new AST.Grouping(new AST.Literal(20.0));

        System.out.println("Unary: " + unary.getClass().getSimpleName());
        System.out.println("Grouping: " + grouping.getClass().getSimpleName());
    }
}
