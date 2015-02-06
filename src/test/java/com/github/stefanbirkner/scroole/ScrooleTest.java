package com.github.stefanbirkner.scroole;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.rules.ExpectedException.none;

@RunWith(Enclosed.class)
public class ScrooleTest {
    @RunWith(Parameterized.class)
    public static class WritesSourceCode {
        @Parameterized.Parameters(name = "{0}")
        public static Object[] data() {
            return new Object[]{"Simple"};
        }

        @Parameterized.Parameter(0)
        public String name;

        @Rule
        public final TemporaryFolder folder = new TemporaryFolder();

        private File directoryWithScrooleFiles;
        private File outputDirectory;

        @Before
        public void createDirectories() throws IOException {
            directoryWithScrooleFiles = folder.newFolder();
            outputDirectory = folder.newFolder();
        }

        @Test
        public void create_java_file_for_scroole_file() throws Exception {
            createScrooleFile();
            runScroole();
            verifyGeneratedJavaFile();
        }

        private void createScrooleFile() throws IOException {
            String scrooleCode = readResource(name + ".scroole");
            File file = new File(directoryWithScrooleFiles, name + ".scroole");
            write(file, scrooleCode);
        }

        private void runScroole() throws IOException {
            new Scroole(directoryWithScrooleFiles, outputDirectory)
                    .generateFiles();
        }

        private void verifyGeneratedJavaFile() throws IOException {
            File javaFile = new File(outputDirectory, name + ".java");
            String javaCode = readFileToString(javaFile);
            assertThat(javaCode).isEqualTo(readResource(name + ".java"));
        }
    }

    public static class CreatesFiles {
        @Rule
        public final TemporaryFolder folder = new TemporaryFolder();

        private File directoryWithScrooleFiles;
        private File outputDirectory;

        @Before
        public void createDirectories() throws IOException {
            directoryWithScrooleFiles = folder.newFolder();
            outputDirectory = folder.newFolder();
        }

        @Test
        public void generates_java_file_for_class_without_package()
                throws Exception {
            createFileInDirectory(directoryWithScrooleFiles, "Simple.scroole");
            runScroole();
            assertThat(contentOfFile("Simple.java")).doesNotContain("package");
        }

        @Test
        public void generates_java_file_for_class_with_package()
                throws Exception {
            File subDirectory = new File(directoryWithScrooleFiles, "sub");
            createFileInDirectory(subDirectory, "Simple.scroole");
            runScroole();
            assertThat(contentOfFile("sub", "Simple.java"))
                    .startsWith("package sub;");
        }

        @Test
        public void generates_java_files_for_every_scroole_file()
                throws Exception {
            createFileInDirectory(directoryWithScrooleFiles, "Simple.scroole");
            createFileInDirectory(directoryWithScrooleFiles, "Simple2.scroole");
            runScroole();
            assertThat(javaFile("Simple.java")).exists();
            assertThat(javaFile("Simple2.java")).exists();
        }

        @Test
        public void creates_output_directory_if_it_does_not_exist()
                throws Exception {
            createFileInDirectory(directoryWithScrooleFiles, "Simple.scroole");
            File notExistingOutputDirectory = new File(
                    outputDirectory, "not-existing");
            Scroole generator = new Scroole(
                    directoryWithScrooleFiles, notExistingOutputDirectory);
            generator.generateFiles();
            assertThat(notExistingOutputDirectory).exists();
        }

        private void createFileInDirectory(File directory, String name)
                throws IOException {
            String scrooleCode = readResource("Simple.scroole");
            File file = new File(directory, name);
            write(file, scrooleCode);
        }

        private void runScroole() throws IOException {
            Scroole generator = new Scroole(
                    directoryWithScrooleFiles, outputDirectory);
            generator.generateFiles();
        }

        private String contentOfFile(String... path) throws IOException {
            File file = javaFile(path);
            return readFileToString(file);
        }

        private File javaFile(String... path) {
            File file = outputDirectory;
            for (String partOfPath : path)
                file = new File(file, partOfPath);
            return file;
        }
    }

    public static class ValidatesConstructorArguments {
        @Rule
        public final TemporaryFolder folder = new TemporaryFolder();
        @Rule
        public final ExpectedException thrown = none();

        @Test
        public void cannot_be_created_without_directory_with_scroole_files()
                throws Exception {
            thrown.expect(NullPointerException.class);
            new Scroole(null, existingDirectory());
        }

        @Test
        public void cannot_be_created_with_directory_with_scroole_files_being_not_a_directoy()
                throws Exception {
            thrown.expect(IllegalArgumentException.class);
            File file = folder.newFile();
            new Scroole(file, existingDirectory());
        }

        @Test
        public void cannot_be_created_with_non_existing_directory_with_scroole_files()
                throws Exception {
            thrown.expect(FileNotFoundException.class);
            File nonExistingDirectory = new File(existingDirectory(),
                    "not-existing");
            new Scroole(nonExistingDirectory, existingDirectory());
        }

        @Test
        public void cannot_be_created_without_output_directory()
                throws Exception {
            thrown.expect(NullPointerException.class);
            new Scroole(existingDirectory(), null);
        }

        @Test
        public void cannot_be_created_with_output_directory_being_not_a_directoy()
                throws Exception {
            thrown.expect(IllegalArgumentException.class);
            File file = folder.newFile();
            new Scroole(existingDirectory(), file);
        }

        private File existingDirectory() throws IOException {
            return folder.newFolder();
        }
    }

    private static String readResource(String name) throws IOException {
        return IOUtils.toString(ScrooleTest.class.getResourceAsStream(name));
    }
}
