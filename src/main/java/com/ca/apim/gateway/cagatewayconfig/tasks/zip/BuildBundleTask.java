/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class BuildBundleTask extends DefaultTask {

    private final DocumentFileUtils documentFileUtils;
    private final DocumentTools documentTools;
    private final JsonTools jsonTools;
    private final FileUtils fileUtils;
    private DirectoryProperty from;
    private RegularFileProperty into;
    private ConfigurableFileCollection dependencies;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public BuildBundleTask() {
        this(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE, FileUtils.INSTANCE, JsonTools.INSTANCE);
    }

    private BuildBundleTask(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, FileUtils fileUtils, final JsonTools jsonTools) {
        into = newOutputFile();
        from = newInputDirectory();
        dependencies = getProject().files();

        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
        this.fileUtils = fileUtils;
    }

    @InputDirectory
    public DirectoryProperty getFrom() {
        return from;
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencies() {
        return dependencies;
    }

    @TaskAction
    public void perform() {
        BundleBuilder bundleBuilder = new BundleBuilder(documentTools, documentFileUtils, fileUtils, jsonTools);
        bundleBuilder.buildBundle(from.getAsFile().get(), into.getAsFile().get().toPath(), dependencies.getFiles());
    }
}
