/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin;

import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.buildBundleItemKey;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.buildBundleMappingKey;
import static com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils.METADATA_FILE_NAME_SUFFIX;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.*;

class CAGatewayDeveloperTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayDeveloperTest.class.getName());
    private final String projectVersion = "-1.2.3-SNAPSHOT";
    private final String POLICY_INSTALL_BUNDLE_SUFFIX = INSTALL_BUNDLE_EXTENSION;
    private final String ENV_INSTALL_BUNDLE_SUFFIX = INSTALL_BUNDLE_EXTENSION;
    private final String ENV_DELETE_BUNDLE_SUFFIX = DELETE_BUNDLE_EXTENSION;
    private final String ENV_PREFIX = "-environment";

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProject(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        String bundleFileName = projectFolder + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        validateBuildDir(projectFolder, bundleFileName, buildDir);
        File buildGatewayDir = new File(buildDir, "gateway");
        File buildGatewayBundlesDir = new File(buildGatewayDir, "bundle");
        File builtBundleFile = new File(buildGatewayBundlesDir,  bundleFileName);
        assertTrue(builtBundleFile.isFile());
        String deleteBundleFileName = projectFolder + projectVersion + DELETE_BUNDLE_EXTENSION;
        File builtDeleteBundleFile = new File(buildGatewayBundlesDir,  deleteBundleFileName);
        assertTrue(builtDeleteBundleFile.isFile());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectCustomOrganization(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project-custom-organization";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildGatewayDir = new File(testProjectDir, "dist");
        assertTrue(buildGatewayDir.isDirectory());
        String bundleFileName = projectFolder + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        File builtBundleFile = new File(buildGatewayDir, bundleFileName);
        assertTrue(builtBundleFile.isFile());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testMultiProject(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "multi-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertMultiProject(testProjectDir, result);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testMultiProjectBuildingEnvironment(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "multi-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", ":project-c:build-environment-bundle", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertMultiProject(testProjectDir, result);
        File projectC_EnvBundle = new File(new File(new File(new File(new File(testProjectDir, "project-c"), "build"), "gateway"), "bundle"), "project-c" + projectVersion + ENV_INSTALL_BUNDLE_SUFFIX);
        assertTrue(projectC_EnvBundle.exists());
        assertFalse(readFileToString(projectC_EnvBundle, defaultCharset()).isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testMultiProjectOverrideEnvironment(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException, DocumentParseException {
        String projectFolder = "multi-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", ":project-e:build-environment-bundle", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        assertMultiProject(testProjectDir, result);
        File projectE_EnvBundle = new File(new File(new File(new File(new File(testProjectDir, "project-e"), "build"), "gateway"), "bundle"), "project-e" + projectVersion + ENV_INSTALL_BUNDLE_SUFFIX);
        assertTrue(projectE_EnvBundle.exists());
        assertFalse(readFileToString(projectE_EnvBundle, defaultCharset()).isEmpty());
        final Element envBundleElement = DocumentTools.INSTANCE.parse(projectE_EnvBundle).getDocumentElement();
        final List<Element> itemList = getChildElements(getSingleChildElement(envBundleElement, REFERENCES), ITEM);
        for (Element item : itemList) {
            final String type = getSingleChildElementTextContent(item, TYPE);
            final String entityName = getSingleChildElementTextContent(item, NAME);

            switch (type) {
                case EntityTypes.STORED_PASSWORD_TYPE:
                    assertEquals("gateway", entityName);
                    final Element storedPasswordElement = getSingleElement(item, STORED_PASSWD);
                    assertEquals("7layer", getSingleChildElementTextContent(storedPasswordElement, PASSWORD));
                    break;
                case EntityTypes.CLUSTER_PROPERTY_TYPE:
                    assertEquals("log.levels", entityName);
                    final Element clusterPropertyElement = getSingleElement(item, CLUSTER_PROPERTY);
                    assertEquals("com.l7tech.level = FINE", getSingleChildElementTextContent(clusterPropertyElement, VALUE));
                    break;
                default:
                    break;
            }
        }
    }

    private void assertMultiProject(File testProjectDir, BuildResult result) throws IOException {
        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-a:build")).getOutcome());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-b:build")).getOutcome());

        String projectAFilename = "project-a" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        String projectBFilename = "project-b" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        String projectCFilename = "project-c" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        String projectDFilename = "project-d" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        validateBuildDir("project-a", projectAFilename, new File(new File(testProjectDir, "project-a"), "build"));
        validateBuildDir("project-b" , projectBFilename, new File(new File(testProjectDir, "project-b"), "build"));
        validateBuildDir("project-c" , projectCFilename, new File(new File(testProjectDir, "project-c"), "build"));
        validateBuildDir("project-d" , projectDFilename, new File(new File(testProjectDir, "project-d"), "build"));

        File projectC_GW7 = new File(new File(new File(new File(testProjectDir, "project-c"), "build"), "gateway"), "project-c" + projectVersion + ".gw7");

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(projectC_GW7)));
        TarArchiveEntry entry;
        Set<String> entries = new HashSet<>();
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            entries.add(entry.getName());
        }
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_1_project-b-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_2_project-d-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_3_project-a-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_4_project-c-1.2.3-SNAPSHOT.install.req.bundle"));
        tarArchiveInputStream.close();
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectWithAssertionsDependencies(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project-with-assertions-dependencies";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        String bundleFileName = projectFolder + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        File gw7 = validateBuildDir(projectFolder, bundleFileName, buildDir);

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(gw7)));
        TarArchiveEntry entry;
        Set<String> entries = new HashSet<>();
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            entries.add(entry.getName());
        }
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_1_my-bundle-1.0.00.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_2_example-project-with-assertions-dependencies-1.2.3-SNAPSHOT-policy.install.req.bundle"));
        assertTrue(entries.contains("opt/SecureSpan/Gateway/runtime/modules/lib/Test-1.0.0.jar"));
        assertTrue(entries.contains("opt/SecureSpan/Gateway/runtime/modules/assertions/Test-2.0.0.aar"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testMultiProjectWithAssertionsDependencies(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "multi-project-with-assertions-dependencies";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-a:build")).getOutcome());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-b:build")).getOutcome());

        String projectAFilename = "project-a" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        String projectBFilename = "project-b" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        String projectCFilename = "project-c" + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        validateBuildDir("project-a" , projectAFilename, new File(new File(testProjectDir, "project-a"), "build"));
        validateBuildDir("project-b" , projectBFilename, new File(new File(testProjectDir, "project-b"), "build"));
        validateBuildDir("project-c" , projectCFilename, new File(new File(testProjectDir, "project-c"), "build"));

        File projectC_GW7 = new File(new File(new File(new File(testProjectDir, "project-c"), "build"), "gateway"), "project-c" + projectVersion + ".gw7");

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(projectC_GW7)));
        TarArchiveEntry entry;
        Set<String> entries = new HashSet<>();
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            entries.add(entry.getName());
        }
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_1_project-a-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_2_project-b-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_3_project-c-1.2.3-SNAPSHOT.install.req.bundle"));
        assertTrue(entries.contains("opt/SecureSpan/Gateway/runtime/modules/lib/Test-1.0.0.jar"));
        tarArchiveInputStream.close();
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectGeneratingEnvironment(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException, DocumentParseException {
        final String projectFolder = "example-project-generating-environment";
        //bundle name given as part of enacass annotation
        final String bundleName = "encass_bundle";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(
                        "build-environment-bundle",
                        "--stacktrace",
                        "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo",
                        "-DconfigFolder=src/main/gateway/config",
                        "-DconfigName=config")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build-environment-bundle")).getOutcome());

        String envBundleFilename = projectFolder + ENV_PREFIX + projectVersion + "-config" + ENV_INSTALL_BUNDLE_SUFFIX;
        File buildDir = new File(testProjectDir, "build");
        File buildGatewayDir = validateBuildDirExceptGW7File(envBundleFilename, buildDir);

        //Environment bundle name format : <bundleName>-<version>-[<configName>]env.install.bundle

        File envBundleFile = new File(new File(buildGatewayDir, "bundle"), envBundleFilename);
        assertTrue(envBundleFile.isFile());

        final Element envBundleElement = DocumentTools.INSTANCE.parse(envBundleFile).getDocumentElement();
        final List<Element> envEntities = getChildElements(getSingleChildElement(envBundleElement, REFERENCES), ITEM);
        assertEquals(5, envEntities.size());
        envEntities.forEach(e -> {
            String type = getSingleChildElementTextContent(e, TYPE);
            String entityName = getSingleChildElementTextContent(e, NAME);
            switch (type) {
                case "SECURE_PASSWORD":
                    assertEquals("gateway", entityName);
                    break;
                case "ID_PROVIDER_CONFIG":
                    assertEquals("Tacoma MSAD", entityName);
                    break;
                case "JDBC_CONNECTION":
                    assertEquals("MySQL", entityName);
                    break;
                case "SSG_CONNECTOR":
                    break;
                default:
                    fail("Unexpected environment value:\n" + DocumentTools.INSTANCE.elementToString(e));
            }
        });
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectGeneratingDeleteBundlesForDeploymentType(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        final String projectFolder = "example-project-generating-environment";
        //bundle name given as part of enacass annotation
        final String bundleName = "encass_bundle";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(
                        "build-environment-bundle",
                        "--stacktrace",
                        "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo",
                        "-DconfigFolder=src/main/gateway/config",
                        "-DconfigName=config")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build-environment-bundle")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        String envDeleteBundleFilename = projectFolder + ENV_PREFIX + projectVersion + "-config" + ENV_DELETE_BUNDLE_SUFFIX;
        File buildGatewayDir = validateBuildDirExceptGW7File(envDeleteBundleFilename, buildDir);


        File builtDeleteEnvBundleFile = new File(new File(buildGatewayDir, "bundle"), envDeleteBundleFilename);
        assertTrue(builtDeleteEnvBundleFile.isFile());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectGeneratingDeleteBundles(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        final String projectFolder = "example-project-generating-environment";
        //bundle name given as part of enacass annotation
        final String bundleName = "encass_bundle";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(
                        "build-environment-bundle",
                        "--stacktrace",
                        "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo",
                        "-DconfigFolder=src/main/gateway/config",
                        "-DconfigName=config")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build-environment-bundle")).getOutcome());


        //Environment bundle name format : <bundleName>-environment-<version>.(<configName>.).bundle
        String envBundleFilename = projectFolder + ENV_PREFIX + projectVersion + "-config" + ENV_INSTALL_BUNDLE_SUFFIX;
        File buildDir = new File(testProjectDir, "build");
        File buildGatewayDir = validateBuildDirExceptGW7File(envBundleFilename, buildDir);
        File builtBundleFile = new File(new File(buildGatewayDir, "bundle"), envBundleFilename);
        assertTrue(builtBundleFile.isFile());

        String envDeleteBundleFilename = projectFolder + ENV_PREFIX + projectVersion + "-config" + ENV_DELETE_BUNDLE_SUFFIX;
        File builtDeleteEnvBundleFile = new File(new File(buildGatewayDir, "bundle"), envDeleteBundleFilename);
        assertTrue(builtDeleteEnvBundleFile.isFile());

    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectGeneratingEnvironmentMalformedFile(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project-generating-environment";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        assertThrows(UnexpectedBuildFailure.class, () -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build-environment-bundle", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo",
                        "-DconfigFolder=src/main/gateway/config/jdbc_malformed",
                        "-DconfigName=jdbc_malformed")
                .withPluginClasspath()
                .withDebug(true)
                .build());
    }

    private File validateBuildDir(String projectName, String bundleFilename, File buildDir) {
        File buildGatewayDir = validateBuildDirExceptGW7File(bundleFilename, buildDir);
        File gw7PackageFile = new File(buildGatewayDir, projectName + projectVersion + ".gw7");
        assertTrue(gw7PackageFile.isFile());
        return gw7PackageFile;
    }

    @NotNull
    private File validateBuildDirExceptGW7File(String bundleFileName, File buildDir) {
        assertTrue(buildDir.isDirectory());
        File buildGatewayDir = new File(buildDir, "gateway");
        assertTrue(buildGatewayDir.isDirectory());
        File buildGatewayBundlesDir = new File(buildGatewayDir, "bundle");
        assertTrue(buildGatewayBundlesDir.isDirectory());
        File builtBundleFile = new File(buildGatewayBundlesDir, bundleFileName);
        assertTrue(builtBundleFile.isFile());
        return buildGatewayDir;
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectGeneratingFullBundle(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException, DocumentParseException {
        String projectFolder = "example-project-generating-environment";
        //bundle name given as part of enacass annotation
        final String bundleName = "encass_bundle";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(
                        "build-full-bundle",
                        "--stacktrace",
                        "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo",
                        "-DconfigFolder=src/main/gateway/config",
                        "-DconfigName=config")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build-bundle")).getOutcome());
        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build-full-bundle")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        String bundleFilename = bundleName + projectVersion + POLICY_INSTALL_BUNDLE_SUFFIX;
        File buildGatewayDir = validateBuildDirExceptGW7File(bundleFilename, buildDir);

        //Deployment bundle name format : <bundleName>-<version>.bundle
        File builtBundleFile = new File(new File(buildGatewayDir, "bundle"), bundleFilename);
        assertTrue(builtBundleFile.isFile());
        final Element bundleElement = DocumentTools.INSTANCE.parse(builtBundleFile).getDocumentElement();
        final Set<String> bundleItemsIds = getChildElements(getSingleChildElement(bundleElement, REFERENCES), ITEM).stream().map(EnvironmentBundleUtils::buildBundleItemKey).collect(toSet());
        final Set<String> bundleMappingsIds = getChildElements(getSingleChildElement(bundleElement, MAPPINGS), MAPPING).stream().map(EnvironmentBundleUtils::buildBundleMappingKey).collect(toSet());
        final Element dependencyBundle = DocumentTools.INSTANCE.parse(new File(new File(testProjectDir, "lib"), "my-bundle-1.0.00.bundle")).getDocumentElement();
        bundleItemsIds.addAll(getChildElements(getSingleChildElement(dependencyBundle, REFERENCES), ITEM).stream().map(EnvironmentBundleUtils::buildBundleItemKey).collect(toSet()));
        bundleMappingsIds.addAll(getChildElements(getSingleChildElement(dependencyBundle, MAPPINGS), MAPPING).stream().map(EnvironmentBundleUtils::buildBundleMappingKey).collect(toSet()));

        //Full bundle name format : <bundleName>-<version>-full.install.bundle
        File builtFullBundleFile = new File(new File(buildGatewayDir, "bundle"), bundleName + projectVersion + FULL_INSTALL_BUNDLE_NAME_SUFFIX);
        assertTrue(builtFullBundleFile.isFile());

        final Element fullBundleElement = DocumentTools.INSTANCE.parse(builtFullBundleFile).getDocumentElement();
        getChildElements(getSingleChildElement(fullBundleElement, REFERENCES), ITEM).forEach(e -> {
            boolean isFromDeployment = bundleItemsIds.remove(buildBundleItemKey(e));
            if (!isFromDeployment) {
                String type = getSingleChildElementTextContent(e, TYPE);
                String entityName = getSingleChildElementTextContent(e, NAME);
                switch (type) {
                    case "SECURE_PASSWORD":
                        assertEquals("gateway", entityName);
                        break;
                    case "ID_PROVIDER_CONFIG":
                        assertEquals("Tacoma MSAD", entityName);
                        break;
                    case "JDBC_CONNECTION":
                        assertEquals("MySQL", entityName);
                        break;
                    case "SSG_CONNECTOR":
                        break;
                    default:
                        fail("Unexpected environment value:\n" + DocumentTools.INSTANCE.elementToString(e));
                }
            }
        });
        assertTrue(bundleItemsIds.isEmpty(), "Items on deployment bundle not found on full bundle: " + bundleItemsIds.toString());

        getChildElements(getSingleChildElement(fullBundleElement, MAPPINGS), MAPPING).forEach(e -> {
            boolean isFromDeployment = bundleMappingsIds.remove(buildBundleMappingKey(e));
            if (!isFromDeployment) {
                String type = e.getAttribute(ATTRIBUTE_TYPE);
                switch (type) {
                    case "SECURE_PASSWORD":
                    case "ID_PROVIDER_CONFIG":
                    case "JDBC_CONNECTION":
                    case "SSG_CONNECTOR":
                        break;
                    default:
                        fail("Unexpected environment mapping: " + DocumentTools.INSTANCE.elementToString(e));
                }
            }
        });
        assertTrue(bundleItemsIds.isEmpty(), "Mappings on deployment bundle not found on full bundle: " + bundleMappingsIds.toString());

        // validate the full bundle metadata - dependencies list should be empty
        File bundleMetadataFile = new File(new File(buildGatewayDir, "bundle"), bundleName + projectVersion + PREFIX_FULL + METADATA_FILE_NAME_SUFFIX);
        assertTrue(bundleMetadataFile.isFile());
        final ObjectMapper objectMapper = JsonTools.INSTANCE.getObjectMapper();
        final MapType type = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);
        Map<String, Object> metadataProperties = objectMapper.readValue(bundleMetadataFile, type);
        List<Map<String, String>> dependencies = ((List) metadataProperties.get("dependencies"));
        assertTrue(dependencies.isEmpty());
    }
}