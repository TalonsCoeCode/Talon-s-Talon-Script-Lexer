import java.util.ArrayList;

public class TestParser {
    public static void main(String[] args) {
        test("25 * (3 + 2)");
        test("-25 + 3 * 2");
        test("10 >= 5 == true");
        test("!!!!!(false)");
        test("(3+2) * (4+5)");
        test("none");
        test("3==2==5");
        test("2@16");
        test("(2+8)1");

        System.out.println("--- Error test ---");
        TalonScript.run("25 * (3 + 2");
    }

    private static void test(String source) {
        System.out.println("Source: " + source);
        TalonScript.run(source);
        System.out.println();
    }
}
