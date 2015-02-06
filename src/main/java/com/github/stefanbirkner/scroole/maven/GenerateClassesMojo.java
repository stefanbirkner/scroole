package com.github.stefanbirkner.scroole.maven;

import com.github.stefanbirkner.scroole.Scroole;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

/**
 * Creates Java source files using the Scroole code generator.
 */
@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES,
        requiresProject = true)
public class   GenerateClassesMojo extends AbstractMojo {
    /**
     * Location of the Java source files.
     */
    @Parameter(
            defaultValue = "${project.build.directory}/generated-sources/scroole/",
            required = true)
    private File outputDirectory;

    /**
     * Location of the Scroole files.
     */
    @Parameter(defaultValue = "src/main/java/", required = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        createSourceFiles();
        announceSourceFilesToOtherPlugins();
    }

    private void createSourceFiles() throws MojoExecutionException {
        try {
            Scroole scroole = new Scroole(sourceDirectory, outputDirectory);
            scroole.generateFiles();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not generate source files.",
                    e);
        }
    }

    private void announceSourceFilesToOtherPlugins() {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }
}
