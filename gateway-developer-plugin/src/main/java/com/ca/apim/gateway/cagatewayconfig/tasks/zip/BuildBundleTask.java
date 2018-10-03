/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class BuildBundleTask extends DefaultTask {

    private DirectoryProperty from;
    private DirectoryProperty into;
    private ConfigurableFileCollection dependencies;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public BuildBundleTask() {
        into = newOutputDirectory();
        from = newInputDirectory();
        dependencies = getProject().files();
    }

    @InputDirectory
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
        BundleBuilder bundleBuilder = ConfigBuilderModule.getInjector().getInstance(BundleBuilder.class);
        bundleBuilder.buildBundle(from.getAsFile().get(), into.getAsFile().get(), dependencies.getFiles(), getProject().getName());
    }
}
