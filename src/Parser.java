import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private int current = 0;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public AST parse() {
        try {
            AST result = expression();

            if (!isAtEnd()) {
                error(peek(), "Expected end of expression.");
                return null;
            }

            return result;
        } catch (RuntimeException e) {
            if (!TalonScript.errorState) {
                TalonScript.raiseError("Parser error: " + e.getMessage(), peek().line);
            }
            return null;
        }
    }

    public AST expression() {
        return equality();
    }

    // <equality> -> <comparison> (("!=" | "==") <comparison>)*
    private AST equality() {
        AST ast = comparison();

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            Token operator = previous();
            AST right = comparison();
            if (right == null) return null;
            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    // <comparison> -> <term> ((">" | ">=" | "<" | "<=") <term>)*
    private AST comparison() {
        AST ast = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            AST right = term();
            if (right == null) return null;
            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    // <term> -> <factor> (("+" | "-") <factor>)*
    private AST term() {
        AST ast = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            AST right = factor();
            if (right == null) return null;
            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    // <factor> -> <unary> (("*" | "/") <unary>)*
    private AST factor() {
        AST ast = unary();

        while (match(TokenType.MULT, TokenType.DIV)) {
            Token operator = previous();
            AST right = unary();
            if (right == null) return null;
            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    // <unary> -> ("!" | "-") <unary> | <primary>
    private AST unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            AST right = unary();
            if (right == null) return null;
            return new AST.Unary(operator, right);
        }

        return primary();
    }

    // <primary> -> NUMBER | STRING | "true" | "false" | "none"
    //             | "(" <expression> ")"
    private AST primary() {
        if (match(TokenType.NUMBER, TokenType.STRING,
                TokenType.TRUE, TokenType.FALSE, TokenType.NONE)) {
            return new AST.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            AST expression = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new AST.Grouping(expression);
        }

        error(peek(), "Expected an expression.");
        return null;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        error(peek(), message);
        return null;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(Token token, String message) {
        TalonScript.raiseError(message, token.line);
    }
}
//The parser follows this grammar:
//expression -> equality
// equality -> comparison (("!=" | "==") comparison)*
// comparison -> term ((">" | ">=" | "<" | "<=") term)*
// term -> factor (("+" | "-") factor)*
// factor -> unary (("*" | "/") unary)*
// unary -> ("!" | "-") unary | primary
// primary -> NUMBER | STRING | true | false | none | "(" expression ")"