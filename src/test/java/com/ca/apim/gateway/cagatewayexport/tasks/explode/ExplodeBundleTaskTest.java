package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.util.json.JsonTools.*;
import static org.junit.Assert.assertEquals;

class ExplodeBundleTaskTest {

    private ExplodeBundleTask explodeBundleTask;

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performDefaultYamlExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.perform();
        validateFilesOutputTypeFromDir(exportDir, YAML_FILE_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performJsonExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType(JSON);
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, JSON_FILE_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performYamlExportWhenOutputTypeNotRecognized(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType("xml");
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, YAML_FILE_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performYamlExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType(YAML_FILE_EXTENSION);
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, YAML_FILE_EXTENSION);
    }

    @NotNull
    private File setUpExportDir(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        final File testProjectDir = new File(temporaryFolder.getRoot(), "exportDir");
        final File bundleFile = new File(testProjectDir, "bundle.bundle");
        final String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("bundles/export-test.bundle")).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());
        final Project project = ProjectBuilder.builder().withProjectDir(testProjectDir).build();
        final File exportDir = new File(testProjectDir, "gateway");
        Files.createDirectory(exportDir.toPath());
        explodeBundleTask = project.getTasks().create("export", ExplodeBundleTask.class, t -> {
            t.getInputBundleFile().set(bundleFile);
            t.getExportDir().set(exportDir);
        });
        return exportDir;
    }

    /**
     *
     * @param exportDir The exportDir as a file
     * @param fileExtension either .json or .yaml
     */
    private void validateFilesOutputTypeFromDir(File exportDir, String fileExtension) {
        File configDir = new File(exportDir, "config");
        File[] files = configDir.listFiles();
        Arrays.stream(files)
                //Every config file except properties files should be the outputType specified
                .filter(file -> !file.getName().endsWith(".properties"))
                .collect(Collectors.toList())
                .forEach(file -> assertEquals(file.getName().substring(file.getName().length() - fileExtension.length()), fileExtension));
    }
}