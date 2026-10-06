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

        System.out.println("\n--- Runtime error expressions ---");
        test("\"3\" + 3");
        test("3 + \"cat\"");
        test("\"cat\" - \"dog\"");
        test("-(\"cat\")");
        test("3 > \"cat\"");
        test("8/0");
    }

    private static void test(String source) {
        System.out.println("Source: " + source);
        TalonScript.run(source);
        System.out.println();
    }
}
