/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ExplodeBundleTask extends DefaultTask {

    private Property<String> folderPath;
    private RegularFileProperty inputBundleFile;
    private DirectoryProperty exportDir;
    private final Property<Map> exportEntities;

    @Inject
    public ExplodeBundleTask() {
        folderPath = getProject().getObjects().property(String.class);
        inputBundleFile = newInputFile();
        exportDir = newOutputDirectory();
        exportEntities = getProject().getObjects().property(Map.class);
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

    @Input
    @Optional
    public Property<Map> getExportEntities() {
        return exportEntities;
    }

    @TaskAction
    public void perform() throws DocumentParseException {
        ExplodeBundle explodeBundle = ExportPluginModule.getInjector().getInstance(ExplodeBundle.class);
        checkExportEntities();
        explodeBundle.explodeBundle(folderPath.getOrElse("/"), toFilterConfiguration(exportEntities.getOrElse(Collections.emptyMap())), inputBundleFile.getAsFile().get(), exportDir.getAsFile().get());
    }

    /**
     * Checks that the export entities map is of the correct type. Should be {@code Map<String,Collection<String>> }
     */
    private void checkExportEntities() {
        if (exportEntities.isPresent()) {
            exportEntities.get().forEach((k, v) -> {
                if (!String.class.isAssignableFrom(k.getClass())) {
                    throw new IllegalArgumentException("Expected exportEntities map keys to all be Strings. Found type: '" + k.getClass() + "' for key: " + k);
                }
                if (!Collection.class.isAssignableFrom(v.getClass())) {
                    throw new IllegalArgumentException("Expected exportEntities map values to all be Collections of Strings. Found type: '" + v.getClass() + "' for key: " + k);
                }
                ((Collection) v).forEach(s -> {
                    if (!String.class.isAssignableFrom(s.getClass())) {
                        throw new IllegalArgumentException("Expected exportEntities map values to all be Collections of Strings. Found type: '" + s.getClass() + "' in collection for key: '" + k + "'. It's value is: " + s);
                    }
                });
            });
        }
    }

    private FilterConfiguration toFilterConfiguration(Map<String, Collection<String>> gatewayExportEntities) {
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        filterConfiguration.setEntityFilters(gatewayExportEntities);
        return filterConfiguration;
    }
}
