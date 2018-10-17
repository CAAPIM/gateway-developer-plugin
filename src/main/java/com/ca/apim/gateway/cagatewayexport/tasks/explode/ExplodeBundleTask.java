/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.GatewayExportEntities;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;

import javax.inject.Inject;
import java.util.Collections;

public class ExplodeBundleTask extends DefaultTask {

    private Property<String> folderPath;
    private RegularFileProperty inputBundleFile;
    private DirectoryProperty exportDir;
    private GatewayExportEntities gatewayExportEntities;

    @Inject
    public ExplodeBundleTask() {
        folderPath = getProject().getObjects().property(String.class);
        inputBundleFile = newInputFile();
        exportDir = newOutputDirectory();
        gatewayExportEntities = getProject().getObjects().newInstance(GatewayExportEntities.class, getProject());
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
    public void exportEntities(Action<? super GatewayExportEntities> action) {
        action.execute(gatewayExportEntities);
    }

    @TaskAction
    public void perform() throws DocumentParseException {
        ExplodeBundle explodeBundle = ExportPluginModule.getInjector().getInstance(ExplodeBundle.class);
        explodeBundle.explodeBundle(folderPath.getOrElse("/"), toFilterConfiguration(gatewayExportEntities), inputBundleFile.getAsFile().get(), exportDir.getAsFile().get());
    }

    private FilterConfiguration toFilterConfiguration(GatewayExportEntities gatewayExportEntities) {
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        filterConfiguration.getCertificates().addAll(gatewayExportEntities.getCertificates().getOrElse(Collections.emptySet()));
        filterConfiguration.getClusterProperties().addAll(gatewayExportEntities.getClusterProperties().getOrElse(Collections.emptySet()));
        filterConfiguration.getIdentityProviders().addAll(gatewayExportEntities.getIdentityProviders().getOrElse(Collections.emptySet()));
        filterConfiguration.getJdbcConnections().addAll(gatewayExportEntities.getJdbcConnections().getOrElse(Collections.emptySet()));
        filterConfiguration.getListenPorts().addAll(gatewayExportEntities.getListenPorts().getOrElse(Collections.emptySet()));
        filterConfiguration.getPasswords().addAll(gatewayExportEntities.getPasswords().getOrElse(Collections.emptySet()));
        filterConfiguration.getPrivateKeys().addAll(gatewayExportEntities.getPrivateKeys().getOrElse(Collections.emptySet()));
        return filterConfiguration;
    }
}
