
import java.util.List;

public abstract class AST {

    // Expressions

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

    public static class Variable extends AST {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }
    }

    public static class Assign extends AST {
        public final Token name;
        public final AST value;

        public Assign(Token name, AST value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Logical extends AST {
        public final AST left;
        public final Token operator;
        public final AST right;

        public Logical(AST left, Token operator, AST right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Function Call
    public static class Call extends AST {
        public final AST callee;
        public final Token paren;
        public final List<AST> arguments;

        public Call(AST callee, Token paren, List<AST> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }
    }

    // Statements

    public static class Program extends AST {
        public final List<AST> statements;

        public Program(List<AST> statements) {
            this.statements = statements;
        }
    }

    public static class Var extends AST {
        public final Token name;
        public final AST initializer;

        public Var(Token name, AST initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static class Print extends AST {
        public final AST expression;

        public Print(AST expression) {
            this.expression = expression;
        }
    }

    public static class Expression extends AST {
        public final AST expression;

        public Expression(AST expression) {
            this.expression = expression;
        }
    }

    public static class Block extends AST {
        public final List<AST> statements;

        public Block(List<AST> statements) {
            this.statements = statements;
        }
    }

    public static class If extends AST {
        public final AST condition;
        public final AST thenBranch;
        public final AST elseBranch;

        public If(AST condition, AST thenBranch, AST elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    public static class While extends AST {
        public final AST condition;
        public final AST body;

        public While(AST condition, AST body) {
            this.condition = condition;
            this.body = body;
        }
    }
}
