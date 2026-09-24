public abstract class AST {

    public static class Binary extends AST {
        public final AST left;
        public final Token operator;
        public final AST right;

        public Binary(AST left, Token operator, AST right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Unary extends AST {
        public final Token operator;
        public final AST right;

        public Unary(Token operator, AST right) {
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Literal extends AST {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }
    }

    public static class Grouping extends AST {
        public final AST expression;

        public Grouping(AST expression) {
            this.expression = expression;
        }
    }

}