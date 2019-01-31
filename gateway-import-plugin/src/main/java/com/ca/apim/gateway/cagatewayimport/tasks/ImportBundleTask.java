/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayimport.tasks;

import com.ca.apim.gateway.connection.GatewayClient;
import com.ca.apim.gateway.connection.GatewayConnectionProperties;
import org.apache.http.entity.FileEntity;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.client.methods.HttpPut.METHOD_NAME;
import static org.apache.http.client.methods.RequestBuilder.create;

/**
 * Task to import a bundle into an existing running gateway.
 */
public class ImportBundleTask extends DefaultTask {

    private GatewayClient gatewayClient;

    //Inputs
    private GatewayConnectionProperties gatewayConnectionProperties;
    private RegularFileProperty importFile;

    public ImportBundleTask() {
        this.gatewayClient = GatewayClient.INSTANCE;
        gatewayConnectionProperties = new GatewayConnectionProperties(getProject());
        importFile = newInputFile();

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
     * @return the bundle file to be imported
     */
    public RegularFileProperty getImportFile() {
        return importFile;
    }

    @TaskAction
    public void perform() {
        File bundleFile = importFile.getAsFile().get();
        gatewayClient.makeGatewayAPICall(
                create(METHOD_NAME)
                        .setUri(gatewayConnectionProperties.getRestmanBundleEndpoint())
                        .setEntity(new FileEntity(bundleFile))
                        .setHeader(CONTENT_TYPE, "application/xml"),
                gatewayConnectionProperties.getUserName().get(),
                gatewayConnectionProperties.getUserPass().get()
        );
    }
}
