/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.export;

import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClient;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.apache.http.client.HttpClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.InputStream;
import java.util.*;

public class BuildExportQueryTask extends DefaultTask {
    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";
    private DocumentTools documentTools;
    private GatewayClient gatewayClient;

    //Inputs
    private GatewayConnectionProperties gatewayConnectionProperties;

    //Outputs
    private Property<String> exportQuery;

    @Inject
    public BuildExportQueryTask() {
        this(GatewayClient.INSTANCE, DocumentTools.INSTANCE);
    }

    private BuildExportQueryTask(final GatewayClient gatewayClient, final DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.gatewayClient = gatewayClient;
        gatewayConnectionProperties = new GatewayConnectionProperties(getProject());
        exportQuery = getProject().getObjects().property(String.class);

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
     * The path of the folder to export.
     * @return The generated export query
     */
    @Internal
    public Property<String> getExportQuery() {
        return exportQuery;
    }

    /**
     * Used in Unit Tests only
     */
    void setDocumentTools(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    /**
     * Used in Unit Tests only
     */
    void setGatewayClient(GatewayClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    @TaskAction
    public void perform() {
        final List<String> exportFolderIds = gatewayClient.makeGatewayAPICallsWithReturn(
                client -> getFolderPathIds(gatewayConnectionProperties.getFolderPath().get(), client),
                gatewayConnectionProperties.getUserName().get(),
                gatewayConnectionProperties.getUserPass().get());

        exportQuery.set(buildQuery(exportFolderIds));
    }

    private String buildQuery(final List<String> exportFolderIds) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?encassAsPolicyDependency=true");
        exportFolderIds.forEach(s -> stringBuilder.append("&folder=").append(s));
        return stringBuilder.toString();
    }

    private List<String> getFolderPathIds(final String folderPath, final HttpClient client) {
        final List<String> pathSegments = getFolderPathSegments(folderPath);
        final LinkedList<String> folderIds = new LinkedList<>();
        folderIds.add(ROOT_FOLDER_ID);
        String parentFolderName = "Root Folder";
        for (final String segment : pathSegments) {
            final String uri = gatewayConnectionProperties.getUrl().get() + "/1.0/folders?parentFolder.id=" + folderIds.getLast() + "&name=" + segment;
            final InputStream inputStream = gatewayClient.makeAPICall(client, uri);
            folderIds.add(getFolderIdFromRestmanResponse(segment, parentFolderName, inputStream));
            parentFolderName = segment;
        }
        return folderIds;
    }

    String getFolderIdFromRestmanResponse(String folderName, String parentFolderName, final InputStream response) {
        final XPath xPath = documentTools.newXPath();
        final NodeList nodeList;
        try {
            final Document doc = documentTools.parse(response);
            nodeList = (NodeList) xPath.evaluate("/List/Item/Id/text()", doc, XPathConstants.NODESET);
        } catch (DocumentParseException | XPathExpressionException e) {
            throw new ExportException("Could not retrieve folder Id. Unable to parse document", e);
        }
        if (nodeList.getLength() < 1) {
            throw new ExportException(String.format("Could not retrieve folder Id. Folder not found with name: '%s' and parent: '%s'", folderName, parentFolderName));
        } else if (nodeList.getLength() > 1) {
            throw new ExportException(String.format("Could not retrieve folder Id. Multiple folders found with name: '%s' and parent: '%s'", folderName, parentFolderName));
        } else {
            return nodeList.item(0).getTextContent();
        }
    }

    static List<String> getFolderPathSegments(final String folderPath) {
        if (!folderPath.startsWith("/")) {
            throw new ExportException("Folder Path must start with '/'");
        }
        final String[] paths = folderPath.split("/");
        if (paths.length == 0) {
            return Collections.emptyList();
        } else {
            final List<String> pathSegments = new ArrayList<>(paths.length - 1);
            pathSegments.addAll(Arrays.asList(paths).subList(1, paths.length));
            return pathSegments;
        }
    }
}
