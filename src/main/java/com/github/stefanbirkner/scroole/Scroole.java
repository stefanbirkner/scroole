package com.github.stefanbirkner.scroole;

import com.github.stefanbirkner.scroole.model.ClassSpecification;
import com.github.stefanbirkner.scroole.model.Parser;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * Generates a Java source file for each Scroole file in a directory.
 */
public class Scroole {
    private static final CodeGenerator CODE_GENERATOR = new CodeGenerator();
    private static final Parser PARSER = new Parser();
    private static final String SCROOLE_FILE_SUFFIX = "scroole";
    private static final boolean RECURSIVE = true;
    private File directoryWithScrooleFiles;
    private File outputDirectory;

    /**
     * Create a Scroole code generator.
     *
     * @param directoryWithScrooleFiles the directory that stores the Scroole
     *                                  files.
     * @param outputDirectory the Java files are written to this directory.
     * @throws FileNotFoundException if {@code directoryWithScrooleFiles} does
     * not exist.
     */
    public Scroole(File directoryWithScrooleFiles,
            File outputDirectory)
            throws FileNotFoundException {
        this.directoryWithScrooleFiles = validDirectoryWithScrooleFiles(
                directoryWithScrooleFiles);
        this.outputDirectory = isValidOutputDirectory(outputDirectory);
    }

    private File validDirectoryWithScrooleFiles(File directory)
            throws FileNotFoundException {
        Validate.notNull(directory,
                "The argument directoryWithScrooleFiles is null.");
        if (directory.exists()) {
            return validateDirectory(directory, "directory with Scroole files");
        } else {
            throw new FileNotFoundException("The directory with Scroole files "
                    + directory + " does not exist.");
        }
    }

    private File isValidOutputDirectory(File directory)
            throws FileNotFoundException {
        Validate.notNull(directory, "The argument outputDirectory is null.");
        if (directory.exists())
            return validateDirectory(directory, "output directory");
        else
            return directory;
    }

    private File validateDirectory(File directory, String name) {
        if (directory.isDirectory())
            return directory;
        else
            throw new IllegalArgumentException("The " + name + " " + directory
                    + " is not a directory.");
    }

    /**
     * Generate a Java file for each Scroole file.
     */
    public void generateFiles() throws IOException {
        for (File file : getScrooleFiles())
            generateJavaFile(file);
    }

    private Collection<File> getScrooleFiles() {
        return listFiles(directoryWithScrooleFiles,
                new String[]{SCROOLE_FILE_SUFFIX}, RECURSIVE);
    }

    private void generateJavaFile(File file) throws IOException {
        String canonicalName = getClassName(file);
        String description = readFileToString(file);
        writeJavaFileForScrooleFile(canonicalName, description);
    }

    private String getClassName(File file) {
        String localPath = substringAfter(file.getAbsolutePath(),
                directoryWithScrooleFiles.getAbsolutePath());
        return substringBeforeLast(localPath, "." + SCROOLE_FILE_SUFFIX)
                .substring(1).replace("/", ".");
    }

    private void writeJavaFileForScrooleFile(String canonicalName,
            String description) throws IOException {
        ClassSpecification model = PARSER.parse(canonicalName, description);
        String code = CODE_GENERATOR.createCode(model);
        String filename = canonicalName.replace(".", "/") + ".java";
        File outputFile = new File(outputDirectory, filename);
        write(outputFile, code);
    }
}
