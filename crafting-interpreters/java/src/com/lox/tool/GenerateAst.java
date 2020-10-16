package com.lox.tool;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
        ));

    }

    private static void defineAst(
        String outputDir,
        String baseName,
        List<String> types
    ) throws FileNotFoundException, UnsupportedEncodingException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter printWriter = new PrintWriter(path, "UTF-8");
        printWriter.println("package com.lox.ast;");
        printWriter.println();
        printWriter.println("import java.util.List;");
        printWriter.println("import com.lox.Token;");
        printWriter.println();
        printWriter.println("abstract class " + baseName + "{");

        // AST subclasses
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(printWriter, baseName, className, fields);
        }

        printWriter.println("}");
        printWriter.close();
    }

    private static void defineType(
        PrintWriter writer,
        String baseName,
        String className,
        String fieldList
    ) {
        writer.println();
        writer.println("\tstatic class " + className + " extends " + baseName + " {");
        // Constructor.
        writer.println("\t\t" + className + "(" + fieldList + ") {");
        // Store paramters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }
        writer.println("\t\t}");
        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("\t\tfinal " + field + ";");
        }
        writer.println("\t}");
    }
}
