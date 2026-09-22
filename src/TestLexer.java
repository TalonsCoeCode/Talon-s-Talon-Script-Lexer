public class TestLexer {
    public static void main(String[] args) {
        String code = """
            print("Hello");
            x = 123;
            // this is a comment
            y = x + 5;
        """;

        Lexer lexer = new Lexer(code);
        var tokens = lexer.getTokens();

        for (Token t : tokens) {
            System.out.println(t);
        }
    }
}
