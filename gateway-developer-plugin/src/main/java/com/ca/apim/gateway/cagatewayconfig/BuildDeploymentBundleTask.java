/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.*;

import javax.inject.Inject;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.filterBundleFiles;

/**
 * The BuildDeploymentBundle task will take local source files and create a deployment bundle document that can be bootstrapped into a gateway container
 * or pushed via restman to an appliance gateway.
 */
public class BuildDeploymentBundleTask extends DefaultTask {

    private DirectoryProperty from;
    private DirectoryProperty into;
    private ConfigurableFileCollection dependencies;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public BuildDeploymentBundleTask() {
        into = newOutputDirectory();
        from = newInputDirectory();
        dependencies = getProject().files();
    }

    @InputDirectory
    @Optional
    public DirectoryProperty getFrom() {
        return from;
    }

    @OutputDirectory
    public DirectoryProperty getInto() {
        return into;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencies() {
        return dependencies;
    }

    @TaskAction
    public void perform() {
        BundleFileBuilder bundleFileBuilder = InjectionRegistry.getInjector().getInstance(BundleFileBuilder.class);
        bundleFileBuilder.buildBundle(from.isPresent() ? from.getAsFile().get() : null, into.getAsFile().get(), filterBundleFiles(dependencies.getFiles()), getProject().getName() + '-' + getProject().getVersion());
    }
}
