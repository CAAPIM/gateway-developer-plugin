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
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class PackageTask extends DefaultTask {

    private ConfigurableFileCollection templatizedBundles;
    private ConfigurableFileCollection scripts;
    private RegularFileProperty into;

    private final FileUtils fileUtils;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public PackageTask() {
        this(FileUtils.INSTANCE);
    }

    private PackageTask(final FileUtils fileUtils) {
        into = newOutputFile();
        templatizedBundles = getProject().files();
        scripts = getProject().files();

        this.fileUtils = fileUtils;
    }

    @InputFiles
    public ConfigurableFileCollection getTemplatizedBundles() {
        return templatizedBundles;
    }

    @InputFiles
    public ConfigurableFileCollection getScripts() {
        return scripts;
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @TaskAction
    public void perform() {
        Packager packager = new Packager(fileUtils);
        packager.buildPackage(into.getAsFile().get(), templatizedBundles.getFiles(), scripts.getFiles());
    }
}
