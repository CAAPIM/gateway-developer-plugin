/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.importing;

import com.ca.apim.gateway.cagatewayexport.config.GatewayConnectionProperties;
import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClient;
import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClientException;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.net.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule.getInstance;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.readFileToString;
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
        this.gatewayClient = getInstance(GatewayClient.class);
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
        try {
            gatewayClient.makeGatewayAPICall(
                    create(METHOD_NAME)
                            .setUri(gatewayConnectionProperties.getFullRestmanURL())
                            .setEntity(new StringEntity(readFileToString(bundleFile, defaultCharset())))
                            .setHeader(CONTENT_TYPE, "application/xml"),
                    gatewayConnectionProperties.getUserName().get(),
                    gatewayConnectionProperties.getUserPass().get()
            );
        } catch (IOException e) {
            throw new GatewayClientException("Could not read file " + bundleFile.getName(), e);
        }
    }
}
