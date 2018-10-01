/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
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

    private Property<String> folderPath;
    private RegularFileProperty inputBundleFile;
    private DirectoryProperty exportDir;

    @Inject
    public ExplodeBundleTask() {
        folderPath = getProject().getObjects().property(String.class);
        inputBundleFile = newInputFile();
        exportDir = newOutputDirectory();
        JsonTools.INSTANCE.setOutputType(JsonTools.YAML);
        getOutputs().upToDateWhen(t -> false);
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
        JsonTools.INSTANCE.setOutputType(format);
    }

    @TaskAction
    public void perform() throws DocumentParseException {
        ExplodeBundle explodeBundle = ExportPluginModule.getInjector().getInstance(ExplodeBundle.class);
        explodeBundle.explodeBundle(folderPath.getOrElse("/"), inputBundleFile.getAsFile().get(), exportDir.getAsFile().get());
    }
}
