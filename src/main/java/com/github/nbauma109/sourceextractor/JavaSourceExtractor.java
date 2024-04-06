package com.github.nbauma109.sourceextractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

public class JavaSourceExtractor {

    private static void extractAndSaveTypes(Path filePath) throws IOException {
        String fileWithoutExtension = removeFileExtension(filePath.getFileName().toString());
        Path parentPath = Optional.ofNullable(filePath.getParent()).orElse(Paths.get("."));
        CompilationUnit compilationUnit = StaticJavaParser.parse(filePath);

        PrinterConfiguration printerConfig = new DefaultPrinterConfiguration();
        printerConfig.removeOption(new DefaultConfigurationOption(ConfigOption.PRINT_COMMENTS));

        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            String typeName = type.getNameAsString();

            if (type.isPublic() || fileWithoutExtension.equals(typeName)) continue;

            int lineStart = type.getBegin().map(pos -> pos.line).orElse(1);
            String padding = "\n".repeat(lineStart - 1);
            String extractedContent = padding + type.toString(printerConfig);

            Path newFilePath = parentPath.resolve(typeName + ".java");

            System.out.println("Extracting: " + newFilePath);
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(newFilePath))) {
                writer.print(extractedContent);
            }
        }
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
    
    private static void processDirectory(Path sourceDir) {
        try {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        extractAndSaveTypes(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.err.println("Failed to visit file: " + file + " due to " + exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <source folder>");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        if (Files.isDirectory(path)) {
            processDirectory(path);
        } else {
            System.out.println("The path provided is not a directory.");
        }
    }
}
