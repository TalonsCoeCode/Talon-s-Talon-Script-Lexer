
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final ArrayList<Token> tokens;
    private int current = 0;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public AST parse() {
        try {
            List<AST> statements = new ArrayList<>();

            while (!isAtEnd()) {
                statements.add(declaration());
            }

            return new AST.Program(statements);

        } catch (ParseError error) {
            return null;
        }
    }

    // declaration -> varDeclaration | statement
    private AST declaration() {
        if (match(TokenType.VAR)) {
            return varDeclaration();
        }

        return statement();
    }

    private AST varDeclaration() {
        Token name = consume(
                TokenType.IDENTIFIER,
                "Expected variable name."
        );

        AST initializer = null;

        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON,
                "Expected ';' after variable declaration.");

        return new AST.Var(name, initializer);
    }

    // statement -> print | if | while | for | block | expression
    private AST statement() {

        if (match(TokenType.WRITE)) {
            return printStatement();
        }

        if (match(TokenType.IF)) {
            return ifStatement();
        }

        if (match(TokenType.WHILE)) {
            return whileStatement();
        }

        if (match(TokenType.FOR)) {
            return forStatement();
        }

        if (match(TokenType.OPEN_BRACE)) {
            return new AST.Block(block());
        }

        return expressionStatement();
    }

    private AST printStatement() {
        AST value = expression();

        consume(TokenType.SEMICOLON,
                "Expected ';' after value.");

        return new AST.Print(value);
    }

    private AST expressionStatement() {
        AST expression = expression();

        consume(TokenType.SEMICOLON,
                "Expected ';' after expression.");

        return new AST.Expression(expression);
    }

    // block -> "{" declaration* "}"
    private List<AST> block() {
        List<AST> statements = new ArrayList<>();

        while (!check(TokenType.CLOSE_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.CLOSE_BRACE,
                "Expected '}' after block.");

        return statements;
    }

    // if and elif statements
    private AST ifStatement() {
        consume(TokenType.LEFT_PAREN,
                "Expected '(' after if.");

        AST condition = expression();

        consume(TokenType.RIGHT_PAREN,
                "Expected ')' after condition.");

        AST thenBranch = statement();
        AST elseBranch = null;

        if (match(TokenType.ELIF)) {
            elseBranch = ifStatement();
        } else if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new AST.If(condition, thenBranch, elseBranch);
    }

    // while -> while (condition) statement
    private AST whileStatement() {
        consume(TokenType.LEFT_PAREN,
                "Expected '(' after while.");

        AST condition = expression();

        consume(TokenType.RIGHT_PAREN,
                "Expected ')' after condition.");

        AST body = statement();

        return new AST.While(condition, body);
    }

    // for -> for (initializer; condition; increment) statement
    // Converted internally into a while loop.
    private AST forStatement() {

        consume(TokenType.LEFT_PAREN,
                "Expected '(' after for.");

        AST initializer;

        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        AST condition = null;

        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }

        consume(TokenType.SEMICOLON,
                "Expected ';' after loop condition.");

        AST increment = null;

        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN,
                "Expected ')' after for clauses.");

        AST body = statement();

        if (increment != null) {
            List<AST> statements = new ArrayList<>();
            statements.add(body);
            statements.add(new AST.Expression(increment));
            body = new AST.Block(statements);
        }

        if (condition == null) {
            condition = new AST.Literal(true);
        }

        body = new AST.While(condition, body);

        if (initializer != null) {
            List<AST> statements = new ArrayList<>();
            statements.add(initializer);
            statements.add(body);
            body = new AST.Block(statements);
        }

        return body;
    }

    // Expressions

    public AST expression() {
        return assignment();
    }

    // assignment -> IDENTIFIER "=" assignment | logicOr
    private AST assignment() {
        AST expression = logicOr();

        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            AST value = assignment();

            if (expression instanceof AST.Variable variable) {
                return new AST.Assign(variable.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private AST logicOr() {
        AST expression = logicAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            AST right = logicAnd();

            expression = new AST.Logical(
                    expression, operator, right
            );
        }

        return expression;
    }

    private AST logicAnd() {
        AST expression = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            AST right = equality();

            expression = new AST.Logical(
                    expression, operator, right
            );
        }

        return expression;
    }

    private AST equality() {
        AST ast = comparison();

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            Token operator = previous();
            AST right = comparison();

            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    private AST comparison() {
        AST ast = term();

        while (match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL
        )) {
            Token operator = previous();
            AST right = term();

            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    private AST term() {
        AST ast = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            AST right = factor();

            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    private AST factor() {
        AST ast = unary();

        while (match(TokenType.MULT, TokenType.DIV)) {
            Token operator = previous();
            AST right = unary();

            ast = new AST.Binary(ast, operator, right);
        }

        return ast;
    }

    private AST unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token operator = previous();
            AST right = unary();

            return new AST.Unary(operator, right);
        }

        return primary();
    }

    private AST primary() {

        if (match(
                TokenType.NUMBER,
                TokenType.STRING,
                TokenType.TRUE,
                TokenType.FALSE,
                TokenType.NONE
        )) {
            return new AST.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new AST.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            AST expression = expression();

            consume(TokenType.RIGHT_PAREN,
                    "Expected ')' after expression.");

            return new AST.Grouping(expression);
        }

        error(peek(), "Expected an expression.");
        return null;
    }

    // Parser helper functions

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
        if (check(type)) {
            return advance();
        }

        error(peek(), message);
        return null;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return type == TokenType.EOF;
        }

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
        throw new ParseError();
    }

    private static class ParseError extends RuntimeException {
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