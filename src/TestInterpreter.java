import java.awt.event.ItemEvent;

public class TestInterpreter {
    public static void main(String[] args) {
        System.out.println("--- Valid expressions ---");
        test("25 * (3 + 2)");
        test("\"hello\" + \" world\"");
        test("5 + 3 == 8");
        test("5 / 0 == 8 / 0");
        test("!(false)");
        test("!\"\"");
        test("!0");
        test("none == none");
        test("\"3\" == 3");
        test("(5+3)3");
        test("5/0");
        test("\"s1\" == \"s1\"");

        System.out.println("\n--- Runtime error expressions ---");
        test("\"3\" + 3");
        test("3 + \"cat\"");
        test("\"cat\" - \"dog\"");
        test("-(\"cat\")");
        test("3 > \"cat\"");
        test("8/0");
        test("3" + "\"3\"");
        test("true + false");
        test("3 < \"5\"");
        test("");
        test(" 3 + (9/0)");

        System.out.println("--- Testing clock() ---");
        test("write clock();");

        System.out.println("\n--- Testing input() ---");
        test("var name = input(\"What is your name? \"); write \"Hello, \" + name;");

        System.out.println("\n--- Testing clock() in an expression ---");
        test("write clock() + 10;");

        System.out.println("\n--- Testing function argument errors ---");
        test("clock(5);");
    }



    private static void test(String source) {
        System.out.println("Source: " + source);
        TalonScript.run(source);
        System.out.println();
    }
}
