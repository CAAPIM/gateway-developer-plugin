/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.GW7Builder;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The Packages build a gw7 package that contains an apply environment script and enables templatized
 * bundles to have environment values applied to them.
 */
class Packager {
    private static final String DIRECTORY_OPT_DOCKER_RC_D = "/opt/docker/rc.d/";
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
     */
    void buildPackage(File gw7File, File bundle, LinkedList<File> dependencyBundles, Set<File> containerApplicationDependencies) {
        byte[] applyEnvBytes = getResourceBytes("/scripts/apply-environment.sh");

        AtomicInteger dependencyBundleCounter = new AtomicInteger(1);
        int numBundles = dependencyBundles.size() + 2;
        Set<GW7Builder.PackageFile> packageFiles = Stream.of(
                // adds dependency bundles
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(dependencyBundles.descendingIterator(), Spliterator.ORDERED), false) // reverses the order of the dependencyBundles
                        .map(f -> new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + getFileCounter(numBundles, dependencyBundleCounter.getAndIncrement()) + "_" + convertToReqBundle(f.getName()), f.length(), () -> fileUtils.getInputStream(f))),
                //adds the deployment bundle
                Stream.of(new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + getFileCounter(numBundles, dependencyBundles.size() + 1) + "_" + convertToReqBundle(bundle.getName()), bundle.length(), () -> fileUtils.getInputStream(bundle))),
                //apply-environment.sh script
                Stream.<GW7Builder.PackageFile>builder()
                        .add(new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "apply-environment.sh", applyEnvBytes.length, () -> new ByteArrayInputStream(applyEnvBytes), true))
                        .build(),
                //adds the apply environment jars
                containerApplicationDependencies.stream().map(f -> new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "apply-environment/" + f.getName(), f.length(), () -> fileUtils.getInputStream(f)))
        )
                .flatMap(Function.identity())
                .collect(Collectors.toSet());

        gw7Builder.buildPackage(fileUtils.getOutputStream(gw7File),
                packageFiles);
    }

    private String convertToReqBundle(String bundleFile) {
        return bundleFile.substring(0, bundleFile.length()-BUNDLE_FILE_EXTENSION.length()).concat(REQ_BUNDLE_FILE_EXTENSION);
    }

    private String getFileCounter(int numBundles, int currentBundleNumber) {
        int paddingLevel = numBundles / 10;
        String format = "%0" + numBundles / 10 + "d";
        return paddingLevel > 0 ? String.format(format, currentBundleNumber) : String.valueOf(currentBundleNumber);
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
}
