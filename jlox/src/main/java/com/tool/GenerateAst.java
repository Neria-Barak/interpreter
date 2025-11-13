package com.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expression", Arrays.asList(
            "Assign : Token name, Expression value",
            "Binary : Expression left, Token operator, Expression right",
            "Ternary : Expression left, Token operator1, Expression middle, Token operator2, Expression right",
            "Grouping : Expression expression",
            "Literal : Object value",
            "Unary : Token operator, Expression right",
            "Variable : Token name",
            "Logical : Expression left, Token operator, Expression right",
            "Call : Expression callee, Token paren, List<Expression> arguments",
            "Function : Statement.Function function"
        ));

        defineAst(outputDir, "Statement", Arrays.asList(
            "ExpressionStm : Expression expression",
            "Print : Expression expression",
            "Var : Token name, Expression initializer",
            "Block : List<Statement> statements",
            "If : Expression condition, Statement thenBranch, Statement elseBranch",
            "While : Expression condition, Statement body",
            "Break : Token keyword",
            "Function : Token name, List<Token> params, List<Statement> body",
            "Return : Token keyword, Expression value"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        
        defineVisitor(writer, baseName, types);

        writer.println();

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("\tabstract <T> T accept(Visitor<T> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tinterface Visitor<T> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tT visit" + typeName + baseName + "(" + typeName + " "  +baseName.toLowerCase() + ");");
        }

        writer.println("\t}");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println("\tstatic class " + className + " extends " + baseName + " {");

        // fields
        String[] fieldList = fields.split(", ");
        for (String field : fieldList) {
            writer.println("\t\tfinal " + field + ";");
        }
        writer.println();

        // constructor
        writer.println("\t\t" + className + "(" + fields + ") {");

        for (String field : fieldList) {
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }

        writer.println("\t\t}");

        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<T> T accept(Visitor<T> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + className  + baseName + "(this);");
        writer.println("\t\t}");

        writer.println("\t}");
    }
 }
