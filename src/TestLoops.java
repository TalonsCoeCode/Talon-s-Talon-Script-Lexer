public class TestLoops {
    public static void main(String[] args) {

        System.out.println("--- Variable Assignment ---");
        test("var x = 10; write x;");
        test("x = 25; write x;");

        System.out.println("\n--- Blocks and Scope ---");
        test("var x = 5; { var x = 20; write x; } write x;");

        System.out.println("\n--- If and Else Statements ---");
        test("var age = 20; if (age >= 18) { write \"Adult\"; } else { write \"Minor\"; }");

        System.out.println("\n--- Logical Operations ---");
        test("var age = 20; var hasID = true; if (age >= 18 and hasID) { write \"Entry allowed\"; }");
        test("if (age < 18 or hasID == false) { write \"Entry denied\"; }");

        System.out.println("\n--- While Loops ---");
        test("var count = 1; while (count <= 5) { write count; count = count + 1; }");

        System.out.println("\n--- Elif Statements ---");
        test("var grade = 85; if (grade >= 90) { write \"A\"; } elif (grade >= 80) { write \"B\"; } elif (grade >= 70) { write \"C\"; } else { write \"Needs improvement\"; }");

        System.out.println("\n--- For Loops ---");
        test("for (var i = 1; i <= 5; i = i + 1) { write i; }");

        System.out.println("\n--- All Tests Finished ---");
    }

    private static void test(String source) {
        System.out.println("Source: " + source);
        TalonScript.run(source);
        System.out.println();
    }
}