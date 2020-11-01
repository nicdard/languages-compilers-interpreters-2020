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
            "Ternary  : Expr guard, Expr then, Expr elseBranch",
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
        printWriter.println("import com.lox.Token;");
        printWriter.println();
        printWriter.println("public abstract class " + baseName + " {");
        printWriter.println();
        defineVisitor(printWriter, baseName, types);

        // AST subclasses
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(printWriter, baseName, className, fields);
        }

        printWriter.println();
        // The base accept method.
        printWriter.println("\tpublic abstract <R> R accept(Visitor<R> visitor);");
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
        writer.println("\tpublic static class " + className + " extends " + baseName + " {");
        // Constructor.
        writer.println("\t\tpublic " + className + "(" + fieldList + ") {");
        // Store paramters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }
        writer.println("\t\t}");
        // Visitor pattern.
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t}");
        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("\t\tpublic final " + field + ";");
        }
        writer.println("\t}");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tpublic interface Visitor<R> {");
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName
                    + "(" + typeName + " " + baseName.toLowerCase() + ");"
            );
        }
        writer.println("\t}");
    }
}
