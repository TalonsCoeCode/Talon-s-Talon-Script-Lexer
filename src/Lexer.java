import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String source;
    private final ArrayList<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("none", TokenType.NONE);
        keywords.put("var", TokenType.VAR);
        keywords.put("write", TokenType.WRITE);
        keywords.put("print", TokenType.WRITE);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("func", TokenType.FUNC);
        keywords.put("return", TokenType.RETURN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
    }

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
            case '{' -> addToken(TokenType.OPEN_BRACE);
            case '}' -> addToken(TokenType.CLOSE_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '.' -> addToken(TokenType.DOT);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.MULT);

            case '!' -> addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
            case '=' -> addToken(match('=') ? TokenType.EQUAL : TokenType.ASSIGN);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);

            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) {
                        nextChar();
                    }
                } else {
                    addToken(TokenType.DIV);
                }
            }

            case '&' -> {
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    TalonScript.raiseError("Unexpected character: &", line);
                }
            }

            case '|' -> {
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    TalonScript.raiseError("Unexpected character: |", line);
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

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
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
            if (peek() == '\n') {
                line++;
            }
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
        while (Character.isDigit(peek())) {
            nextChar();
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            nextChar();
            while (Character.isDigit(peek())) {
                nextChar();
            }
        }

        double value = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, value);
    }

    private void getIdentifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            nextChar();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        Object literal = null;
        if (type == TokenType.TRUE) literal = true;
        if (type == TokenType.FALSE) literal = false;
        if (type == TokenType.NONE) literal = null;

        addToken(type, literal);
    }
}
