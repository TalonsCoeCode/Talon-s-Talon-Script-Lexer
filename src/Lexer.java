import java.util.ArrayList;

public class Lexer {

    private final String source;
    private final ArrayList<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public ArrayList<Token> getTokens() {
        while (!isAtEnd()) {
            start = current;
            findNextToken();
        }

        tokens.add(new Token("", null, line, TokenType.EOF));
        return tokens;
    }

    private void findNextToken() {
        char c = nextChar();

        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);

            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) nextChar();
                } else {
                    addToken(TokenType.SLASH);
                }
            }

            case ' ', '\r', '\t' -> { }
            case '\n' -> line++;

            case '"' -> getStringToken();

            default -> {
                if (Character.isDigit(c)) {
                    getNumberToken();
                } else if (Character.isLetter(c) || c == '_') {
                    getIdentifier();
                } else {
                    TalonScript.raiseError("Unexpected character: " + c, line);
                }
            }
        }
    }

    private char nextChar() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(text, literal, line, type));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void getStringToken() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            nextChar();
        }

        if (isAtEnd()) {
            TalonScript.raiseError("Unterminated string.", line);
            return;
        }

        nextChar(); 

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void getNumberToken() {
        while (Character.isDigit(peek())) nextChar();

        if (peek() == '.' && Character.isDigit(peekNext())) {
            nextChar();
            while (Character.isDigit(peek())) nextChar();
        }

        double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void getIdentifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') nextChar();

        String text = source.substring(start, current);

        TokenType type = TokenType.IDENTIFIER; 
        addToken(type);
    }
}
