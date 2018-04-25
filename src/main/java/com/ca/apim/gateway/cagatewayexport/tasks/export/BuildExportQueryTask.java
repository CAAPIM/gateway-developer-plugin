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
import org.w3c.dom.Node;
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
     *
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

        //get all folder ID so that we can build up full folder tree structure
        final List<String> allFolderIds = gatewayClient.makeGatewayAPICallsWithReturn(
                this::getAllFolderIds,
                gatewayConnectionProperties.getUserName().get(),
                gatewayConnectionProperties.getUserPass().get());

        // get all policy backed services since they are not included by default
        final List<String> exportPBSIds = gatewayClient.makeGatewayAPICallsWithReturn(
                this::getPBSIds,
                gatewayConnectionProperties.getUserName().get(),
                gatewayConnectionProperties.getUserPass().get());

        exportQuery.set(buildQuery(exportFolderIds, allFolderIds, exportPBSIds));
    }

    private String buildQuery(final List<String> exportFolderIds, final List<String> allFolderIds, final List<String> exportPBSIds) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?encassAsPolicyDependency=true");
        stringBuilder.append("&includeDependencies=true");
        for (int i = 0; i < exportFolderIds.size() - 1; i++) {
            String exportFolderId = exportFolderIds.get(i);
            stringBuilder.append("&folder=").append(exportFolderId);
            stringBuilder.append("&requireFolder=").append(exportFolderId);
        }
        stringBuilder.append("&folder=").append(exportFolderIds.get(exportFolderIds.size() - 1));
        allFolderIds.stream().filter(s -> !exportFolderIds.contains(s)).forEach(s -> stringBuilder.append("&folder=").append(s).append("&requireFolder=").append(s));
        exportPBSIds.forEach(s -> stringBuilder.append("&requirePolicyBackedService=").append(s));
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

    private List<String> getAllFolderIds(HttpClient httpClient) {
        final String uri = gatewayConnectionProperties.getUrl().get() + "/1.0/folders";
        return getEntityIds(httpClient, uri, "Folder");
    }

    private List<String> getPBSIds(final HttpClient httpClient) {
        final String uri = gatewayConnectionProperties.getUrl().get() + "/1.0/policyBackedServices";
        return getEntityIds(httpClient, uri, "Policy Backed Service");
    }

    private List<String> getEntityIds(HttpClient client, String uri, final String entityType) {
        final LinkedList<String> ids = new LinkedList<>();
        final InputStream inputStream = gatewayClient.makeAPICall(client, uri);

        final Document allEntityDoc;
        try {
            allEntityDoc = documentTools.parse(inputStream);
        } catch (DocumentParseException e) {
            throw new ExportException(String.format("Could not retrieve %s List. Unable to parse document", entityType), e);
        }

        final XPath xPath = documentTools.newXPath();
        final NodeList nodeList = allEntityDoc.getElementsByTagName("l7:Item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final NodeList idNode;
                try {
                    idNode = (NodeList) xPath.evaluate("Id/text()", node, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    throw new ExportException(String.format("Could not retrieve %s Id.", entityType), e);
                }
                if (idNode.getLength() != 1) {
                    throw new ExportException(String.format("Could not retrieve %s Id. Unexpected Item element format.", entityType));
                } else {
                    ids.add(idNode.item(0).getTextContent());
                }
            }
        }
        return ids;
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
