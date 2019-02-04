/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.export;

import com.ca.apim.gateway.cagatewayexport.config.GatewayConnectionProperties;
import com.ca.apim.gateway.connection.GatewayClient;
import com.ca.apim.gateway.connection.GatewayClientException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule.getInstance;
import static com.ca.apim.gateway.connection.GatewayClient.getRestmanBundleEndpoint;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.apache.http.client.methods.HttpGet.METHOD_NAME;
import static org.apache.http.client.methods.RequestBuilder.create;

/**
 * This task will export from a gateway.
 */
public class ExportTask extends DefaultTask {
    private static final Logger LOGGER = Logger.getLogger(ExportTask.class.getName());

    private GatewayClient gatewayClient;

    //Inputs
    private GatewayConnectionProperties gatewayConnectionProperties;
    private Property<String> exportQuery;

    //Outputs
    private RegularFileProperty exportFile;

    public ExportTask() {
        this.gatewayClient = getInstance(GatewayClient.class);
        gatewayConnectionProperties = new GatewayConnectionProperties(getProject());
        exportQuery = getProject().getObjects().property(String.class);
        exportFile = newOutputFile();

        // makes it so that the export is always run
        getOutputs().upToDateWhen(t -> false);
    }

    @Nested
    public GatewayConnectionProperties getGatewayConnectionProperties() {
        return gatewayConnectionProperties;
    }

    public void setGatewayConnectionProperties(GatewayConnectionProperties gatewayConnectionProperties) {
        this.gatewayConnectionProperties = gatewayConnectionProperties;
    }

    /**
     * The export query to export the bundle with
     *
     * @return The export query to export the bundle with
     */
    @Input
    public Property<String> getExportQuery() {
        return exportQuery;
    }

    /**
     * The file to save the exported bundle to.
     *
     * @return The file to save the bundle to
     */
    @OutputFile
    public RegularFileProperty getExportFile() {
        return exportFile;
    }

    /**
     * Used in Unit Tests only
     */
    void setGatewayClient(GatewayClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    @TaskAction
    public void perform() {
        LOGGER.log(Level.INFO, "Exporting with query: {0}", exportQuery.get());
        File destFile = exportFile.getAsFile().get();
        try {
            copyInputStreamToFile(
                    gatewayClient.makeGatewayAPICall(
                            create(METHOD_NAME).setUri(getRestmanBundleEndpoint(gatewayConnectionProperties.getUrl().get()) + exportQuery.get()),
                            gatewayConnectionProperties.getUserName().get(),
                            gatewayConnectionProperties.getUserPass().get()
                    ),
                    destFile
            );
        } catch (IOException e) {
            throw new GatewayClientException("Could not save response bundle from gateway into file " + destFile.getName(), e);
        }
    }

}
