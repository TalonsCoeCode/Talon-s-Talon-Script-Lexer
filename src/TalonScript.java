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
            String content = java.nio.file.Files.readString(
                java.nio.file.Path.of(filename)
            );
            run(content);
        } catch (java.io.IOException e) {
            System.out.println("Could not read file: " + filename);
        }
    }

    private static void runREPL() {
        java.util.Scanner scan = new java.util.Scanner(System.in);

        while (true) {
            System.out.print("TalonScript > ");

            if (!scan.hasNextLine()) {
                break;
            }

            String line = scan.nextLine();

            if (line.trim().isEmpty()) {
                break;
            }

            run(line);
        }
    }

    public static void run(String source) {
        System.out.println(source);
    }

    public static void raiseError(String msg, int line) {
        System.out.println("Error on line " + line + ": " + msg);
        errorState = true;
    }
}

