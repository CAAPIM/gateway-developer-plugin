package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.*;
import static org.junit.jupiter.api.Assertions.*;

class ExplodeBundleTaskTest {

    private ExplodeBundleTask explodeBundleTask;

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performDefaultYamlExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.perform();
        validateFilesOutputTypeFromDir(exportDir, YML_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performJsonExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType(JSON);
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, JSON_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performYamlExportWhenOutputTypeNotRecognized(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType("xml");
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, YML_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void performYamlExport(TemporaryFolder temporaryFolder) throws Exception {
        File exportDir = setUpExportDir(temporaryFolder);

        explodeBundleTask.setOutputType(YAML);
        explodeBundleTask.perform();

        validateFilesOutputTypeFromDir(exportDir, YML_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void badExportEntitiesKey(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        Map exportEntities = new HashMap<Integer, Collection<String>>();
        exportEntities.put(1, Collections.emptySet());
        setUpExportDir(temporaryFolder, t -> t.getExportEntities().set(exportEntities));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> explodeBundleTask.perform());
        assertTrue(exception.getMessage().contains(Integer.class.getName()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void badExportEntitiesNotACollection(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        Map exportEntities = new HashMap<String, AtomicBoolean>();
        exportEntities.put("passwords", new AtomicBoolean());
        setUpExportDir(temporaryFolder, t -> t.getExportEntities().set(exportEntities));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> explodeBundleTask.perform());
        assertTrue(exception.getMessage().contains(AtomicBoolean.class.getName()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void badExportEntitiesWrongCollectionType(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        Map exportEntities = new HashMap<String, Collection<AtomicLong>>();
        exportEntities.put("passwords", Collections.singleton(new AtomicLong(123L)));
        setUpExportDir(temporaryFolder, t -> t.getExportEntities().set(exportEntities));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> explodeBundleTask.perform());
        assertTrue(exception.getMessage().contains(AtomicLong.class.getName()));
    }

    @NotNull
    private File setUpExportDir(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        return setUpExportDir(temporaryFolder, t -> {
        });
    }

    @NotNull
    private File setUpExportDir(TemporaryFolder temporaryFolder, Consumer<ExplodeBundleTask> taskCustomizer) throws IOException, URISyntaxException {
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
            taskCustomizer.accept(t);
        });
        return exportDir;
    }

    /**
     * @param exportDir     The exportDir as a file
     * @param fileExtension either .json or .yaml
     */
    private void validateFilesOutputTypeFromDir(File exportDir, String fileExtension) {
        File configDir = new File(exportDir, "config");
        File[] files = configDir.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    //Every config file except properties files should be the outputType specified
                    .filter(file -> !file.getName().endsWith(".properties") && !file.isDirectory() && !file.getName().equals("unsupported-entities.xml"))
                    .collect(Collectors.toList())
                    .forEach(file -> assertEquals(FilenameUtils.getExtension(file.getPath()), fileExtension));
        }
    }
}