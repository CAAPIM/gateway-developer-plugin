/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import javax.inject.Inject;

public class ExplodeBundleTask extends DefaultTask {
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;
    private DocumentTools documentTools;

    private Property<String> folderPath;
    private RegularFileProperty inputBundleFile;
    private DirectoryProperty exportDir;

    @Inject
    public ExplodeBundleTask() {
        this(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);
    }

    private ExplodeBundleTask(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, final JsonTools jsonTools) {
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
        folderPath = getProject().getObjects().property(String.class);
        inputBundleFile = newInputFile();
        exportDir = newOutputDirectory();
    }

    /**
     * The path of the folder to explode. This will only explode the contents of the given folder
     *
     * @return The path of the folder to explode.
     */
    @Input
    public Property<String> getFolderPath() {
        return folderPath;
    }

    @InputFile
    public RegularFileProperty getInputBundleFile() {
        return inputBundleFile;
    }

    @OutputDirectory
    public DirectoryProperty getExportDir() {
        return exportDir;
    }

    @Option(option = "outputType", description = "The output type of the configuration files. Either 'yaml' or 'json'.")
    public void setOutputType(String format) {
        jsonTools.setOutputType(format);
    }

    @TaskAction
    public void perform() throws DocumentParseException {
        ExplodeBundle explodeBundle = new ExplodeBundle(documentTools, documentFileUtils, jsonTools);
        explodeBundle.explodeBundle(folderPath.getOrElse("/"), inputBundleFile.getAsFile().get(), exportDir.getAsFile().get());
    }
}
