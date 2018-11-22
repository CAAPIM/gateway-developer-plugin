/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.tasks.gw7.GW7Builder.PackageFile;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * The Packages build a gw7 package that contains an apply environment script and enables templatized
 * bundles to have environment values applied to them.
 */
class Packager {

    private static final String DIRECTORY_OPT_DOCKER_RC_D = "/opt/docker/rc.d/";
    private static final String DIRECTORY_GATEWAY_MODULES = "/opt/SecureSpan/Gateway/runtime/modules/";
    private static final String BUNDLE_FILE_EXTENSION = "bundle";
    private static final String REQ_BUNDLE_FILE_EXTENSION = "req." + BUNDLE_FILE_EXTENSION;

    private final GW7Builder gw7Builder;
    private final FileUtils fileUtils;

    Packager(FileUtils fileUtils, GW7Builder gw7Builder) {
        this.fileUtils = fileUtils;
        this.gw7Builder = gw7Builder;
    }

    /**
     * Builds a gw7 package. The packages will contain utilities to apply environment variables to the solution
     *
     * @param gw7File           the file to output the gw7 package to
     * @param bundle            The bundle file for this solution
     * @param dependencyBundles Dependency bundles to put into the package. These will have their names prefixed so that they get loaded first
     * @param containerApplicationDependencies Dependencies from the environment creator application
     * @param modularAssertionDependencies optional dependent modular assertions
     * @param customAssertionDependencies optional dependent custom assertions
     */
    void buildPackage(File gw7File,
                      File bundle,
                      LinkedList<File> dependencyBundles,
                      Set<File> containerApplicationDependencies,
                      Set<File> modularAssertionDependencies,
                      Set<File> customAssertionDependencies) {

        int numBundles = dependencyBundles.size() + 2;
        Set<PackageFile> packageFiles = Stream.of(
                dependencyBundles(dependencyBundles, numBundles), // adds dependency bundles
                deploymentBundle(bundle, dependencyBundles.size(), numBundles), // adds the deployment bundle
                applyEnvironmentScript(), // apply-environment.sh script
                fileDependencies(containerApplicationDependencies, DIRECTORY_OPT_DOCKER_RC_D + "apply-environment/"), // adds the apply environment jars
                fileDependencies(modularAssertionDependencies, DIRECTORY_GATEWAY_MODULES + "assertions/"), // adds the dependent modular assertions
                fileDependencies(customAssertionDependencies, DIRECTORY_GATEWAY_MODULES + "lib/") // adds the dependent custom assertions
        ).flatMap(identity()).collect(toSet());

        gw7Builder.buildPackage(fileUtils.getOutputStream(gw7File), packageFiles);
    }

    private String convertToReqBundle(String bundleFile) {
        return bundleFile.substring(0, bundleFile.length()-BUNDLE_FILE_EXTENSION.length()).concat(REQ_BUNDLE_FILE_EXTENSION);
    }

    private String getFileCounter(int numBundles, int currentBundleNumber) {
        int paddingLevel = numBundles / 10;
        String format = "%0" + numBundles / 10 + "d";
        return paddingLevel > 0 ? String.format(format, currentBundleNumber) : String.valueOf(currentBundleNumber);
    }

    private Stream<PackageFile> dependencyBundles(LinkedList<File> dependencyBundles, int numBundles) {
        AtomicInteger dependencyBundleCounter = new AtomicInteger(1);

        return stream(
                spliteratorUnknownSize(
                        dependencyBundles.descendingIterator(),
                        ORDERED
                ),
                false
        ) // reverses the order of the dependencyBundles
                .map(f -> new PackageFile(
                        DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + getFileCounter(numBundles, dependencyBundleCounter.getAndIncrement()) + "_" + convertToReqBundle(f.getName()), f.length(),
                        () -> fileUtils.getInputStream(f))
                );
    }

    @NotNull
    private Stream<PackageFile> deploymentBundle(File deploymentBundleFile, int numDependencyBundles, int numBundles) {
        return Stream.of(new PackageFile(
                DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + getFileCounter(numBundles, numDependencyBundles + 1) + "_" + convertToReqBundle(deploymentBundleFile.getName()), deploymentBundleFile.length(),
                () -> fileUtils.getInputStream(deploymentBundleFile))
        );
    }

    private Stream<PackageFile> applyEnvironmentScript() {
        byte[] applyEnvBytes = getResourceBytes("/scripts/apply-environment.sh");

        return Stream.<PackageFile>builder()
                .add(new PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "apply-environment.sh", applyEnvBytes.length, () -> new ByteArrayInputStream(applyEnvBytes), true))
                .build();
    }

    private byte[] getResourceBytes(String resourcePath) {
        try (InputStream applyEnvStream = getClass().getResourceAsStream(resourcePath)) {
            if (applyEnvStream == null) {
                throw new PackageBuildException("Error loading " + resourcePath + " bytes. Could not find the resource.");
            }
            return IOUtils.toByteArray(applyEnvStream);
        } catch (IOException e) {
            throw new PackageBuildException("Error loading " + resourcePath + " bytes: " + e.getMessage(), e);
        }
    }

    @NotNull
    private Stream<PackageFile> fileDependencies(Set<File> files, String path) {
        return files.stream().map(f -> new PackageFile(
                path + f.getName(), f.length(), () -> fileUtils.getInputStream(f))
        );
    }


}
