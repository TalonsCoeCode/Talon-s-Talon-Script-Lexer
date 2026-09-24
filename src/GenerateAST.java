import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) {
        String grammarFile = args.length > 0 ? args[0] : "grammar.txt";
        String outputFile = args.length > 1 ? args[1] : "AST.java";
        try {
            generate(grammarFile, outputFile);
            System.out.println("Generated " + outputFile);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void generate(String grammarFile, String outputFile) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(grammarFile));
        if (lines.isEmpty()) throw new IllegalArgumentException("Grammar file is empty.");

        String[] base = lines.get(0).trim().split(":", 2);
        if (base.length != 2) throw new IllegalArgumentException("Invalid first grammar line.");

        String baseName = base[0].trim();
        List<ClassDefinition> classes = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(":", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid grammar line: " + line);

            List<Field> fields = new ArrayList<>();
            for (String item : parts[1].trim().split(",")) {
                String[] pieces = item.trim().split("\\s+");
                if (pieces.length != 2) throw new IllegalArgumentException("Invalid field: " + item);
                fields.add(new Field(pieces[0], pieces[1]));
            }
            classes.add(new ClassDefinition(parts[0].trim(), fields));
        }

        StringBuilder out = new StringBuilder();
        out.append("public abstract class ").append(baseName).append(" {\n\n");

        for (ClassDefinition c : classes) {
            out.append("    public static class ").append(c.name)
                    .append(" extends ").append(baseName).append(" {\n");

            for (Field f : c.fields)
                out.append("        public final ").append(f.type).append(" ").append(f.name).append(";\n");

            out.append("\n        public ").append(c.name).append("(");
            for (int i = 0; i < c.fields.size(); i++) {
                if (i > 0) out.append(", ");
                Field f = c.fields.get(i);
                out.append(f.type).append(" ").append(f.name);
            }
            out.append(") {\n");

            for (Field f : c.fields)
                out.append("            this.").append(f.name).append(" = ").append(f.name).append(";\n");

            out.append("        }\n    }\n\n");
        }

        out.append("}\n");
        Files.writeString(Path.of(outputFile), out.toString());
    }

    private static class Field {
        final String type, name;
        Field(String type, String name) { this.type = type; this.name = name; }
    }

    private static class ClassDefinition {
        final String name;
        final List<Field> fields;
        ClassDefinition(String name, List<Field> fields) {
            this.name = name; this.fields = fields;
        }
    }
}
