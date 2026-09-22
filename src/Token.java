public class Token {
    final String lexeme;
    final Object literal;
    final int line;
    final TokenType type;

    public Token(String lexeme, Object literal, int line, TokenType type) {
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.type = type;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal + " (line " + line + ")";
    }
}

