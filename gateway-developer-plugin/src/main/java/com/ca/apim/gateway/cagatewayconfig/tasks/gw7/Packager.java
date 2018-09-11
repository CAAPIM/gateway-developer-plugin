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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Packages build a gw7 package that contains an apply environment script and enables templatized
 * bundles to have environment values applied to them.
 */
class Packager {
    private static final String DIRECTORY_OPT_DOCKER_RC_D = "/opt/docker/rc.d/";

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
     * @param envBundle         The environment bundle for this solution
     * @param dependencyBundles Dependency bundles to put into the package. These will have their names prefixed so that they get loaded first
     */
    void buildPackage(File gw7File, File bundle, File envBundle, Set<File> dependencyBundles) {
        byte[] applyEnvBytes = getApplyEnvironmentScriptBytes();

        gw7Builder.buildPackage(fileUtils.getOutputStream(gw7File),
                Stream.of(
                        //apply-environment.sh script
                        Stream.<GW7Builder.PackageFile>builder()
                                .add(new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "apply-environment.sh", applyEnvBytes.length, () -> new ByteArrayInputStream(applyEnvBytes)))
                                .build(),
                        Stream.of(new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/" + bundle.getName(), bundle.length(), () -> fileUtils.getInputStream(bundle))),
                        Stream.of(new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + envBundle.getName(), envBundle.length(), () -> fileUtils.getInputStream(envBundle))),
                        dependencyBundles.stream().map(f -> new GW7Builder.PackageFile(DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/_" + f.getName(), f.length(), () -> fileUtils.getInputStream(f)))
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toSet()));
    }

    private byte[] getApplyEnvironmentScriptBytes() {
        try (InputStream applyEnvStream = getClass().getResourceAsStream("/scripts/apply-environment.sh")) {
            return IOUtils.toByteArray(applyEnvStream);
        } catch (IOException e) {
            throw new PackageBuildException("Error loading apply-environment.sh script bytes: " + e.getMessage(), e);
        }
    }
}
