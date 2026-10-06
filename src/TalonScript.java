import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class TalonScript {

    static boolean errorState = false;

    public static void main(String[] args) {
        if (args.length > 0) {
            runFile(args[0]);
        } else {
            runREPL();
        }
    }

    private static void runFile(String filename) {
        try {
            String content = Files.readString(Path.of(filename));
            run(content);
        } catch (Exception e) {
            System.out.println("Could not read file: " + filename);
        }
    }

    private static void runREPL() {
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.print("TalonScript > ");

            if (!scan.hasNextLine()) break;

            String line = scan.nextLine();
            if (line.trim().isEmpty()) break;

            run(line);
        }
    }

    public static void run(String source) {
        // Reset errors for each new expression/program.
        errorState = false;

        // 1. Source -> tokens
        Lexer lexer = new Lexer(source);
        ArrayList<Token> tokens = lexer.getTokens();

        if (errorState) {
            System.out.println("Lexing failed.");
            return;
        }

        // 2. Tokens -> AST
        Parser parser = new Parser(tokens);
        AST ast = parser.parse();

        if (errorState || ast == null) {
            System.out.println("Parsing failed.");
            return;
        }

        // 3. AST -> value
        Interpreter interpreter = new Interpreter();
        Object value = interpreter.interpret(ast);

        if (errorState) {
            System.out.println("Execution stopped.");
            return;
        }

        System.out.println("Result: " + formatValue(value));
    }

    private static String formatValue(Object value) {
        if (value == null) return "none";

        if (value instanceof Double number) {
            if (Double.isInfinite(number)) {
                return number > 0 ? "infinity" : "-infinity";
            }
            if (Double.isNaN(number)) return "nan";
            if (number == Math.rint(number)) {
                return Long.toString(number.longValue());
            }
        }

        if (value instanceof String) return "\"" + value + "\"";
        return value.toString();
    }

    public static void raiseError(String msg, int line) {
        System.out.println("Error on line " + line + ": " + msg);
        errorState = true;
    }
}