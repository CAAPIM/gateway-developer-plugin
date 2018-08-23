/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.GW7Builder;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class PackageTask extends DefaultTask {

    private ConfigurableFileCollection dependencyBundles;
    private RegularFileProperty into;
    private RegularFileProperty bundle;
    private RegularFileProperty environmentBundle;

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
        environmentBundle = newInputFile();
        dependencyBundles = getProject().files();

        this.fileUtils = fileUtils;
        this.gw7Builder = gw7Builder;
    }

    @InputFile
    public RegularFileProperty getBundle() {
        return bundle;
    }

    @InputFile
    public RegularFileProperty getEnvironmentBundle() {
        return environmentBundle;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @TaskAction
    public void perform() {
        Packager packager = new Packager(fileUtils, gw7Builder);
        packager.buildPackage(into.getAsFile().get(), bundle.getAsFile().get(), getEnvironmentBundle().getAsFile().get(), dependencyBundles.getFiles());
    }
}
