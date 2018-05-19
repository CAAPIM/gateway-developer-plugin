/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.export;

import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClient;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Inject
    public ExportTask() {
        this(GatewayClient.INSTANCE);
    }

    private ExportTask(GatewayClient gatewayClient) {
        this.gatewayClient = gatewayClient;
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
        gatewayClient.makeGatewayAPICallsNoReturn(
                httpClient -> FileUtils.copyInputStreamToFile(gatewayClient.makeAPICall(httpClient, gatewayConnectionProperties.getUrl().get() + "/1.0/bundle" + exportQuery.get()), exportFile.getAsFile().get()),
                gatewayConnectionProperties.getUserName().get(),
                gatewayConnectionProperties.getUserPass().get());
    }

}
