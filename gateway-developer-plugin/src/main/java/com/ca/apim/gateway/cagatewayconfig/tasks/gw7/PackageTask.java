/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.LinkedList;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class PackageTask extends DefaultTask {

    private ConfigurableFileCollection dependencyBundles;
    private ConfigurableFileCollection containerApplicationDependencies;
    private ConfigurableFileCollection dependencyModularAssertions;
    private ConfigurableFileCollection dependencyCustomAssertions;
    private RegularFileProperty into;
    private RegularFileProperty bundle;

    private final FileUtils fileUtils;
    private final GW7Builder gw7Builder;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public PackageTask() {
        this(FileUtils.INSTANCE, GW7Builder.INSTANCE);
    }

    PackageTask(final FileUtils fileUtils, GW7Builder gw7Builder) {
        into = newOutputFile();
        bundle = newInputFile();
        dependencyBundles = getProject().files();
        containerApplicationDependencies = getProject().files();
        dependencyModularAssertions = getProject().files();
        dependencyCustomAssertions = getProject().files();

        this.fileUtils = fileUtils;
        this.gw7Builder = gw7Builder;
    }

    @InputFile
    public RegularFileProperty getBundle() {
        return bundle;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @InputFiles
    public ConfigurableFileCollection getContainerApplicationDependencies() {
        return containerApplicationDependencies;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyModularAssertions() {
        return dependencyModularAssertions;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyCustomAssertions() {
        return dependencyCustomAssertions;
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @TaskAction
    public void perform() {
        Packager packager = new Packager(fileUtils, gw7Builder);
        packager.buildPackage(
                into.getAsFile().get(),
                bundle.getAsFile().get(),
                new LinkedList<>(dependencyBundles.getAsFileTree().getFiles()),
                containerApplicationDependencies.getFiles(),
                dependencyModularAssertions.getFiles(),
                dependencyCustomAssertions.getFiles()
        );
    }
}
