/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.export;

import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClient;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BuildExportQueryTaskTest {

    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";
    @Mock
    private GatewayClient gatewayClient;
    @Mock
    private HttpClient httpClient;

    private BuildExportQueryTask buildExportQueryTask;
    private GatewayConnectionProperties gatewayConnectionProperties;

    @Before
    public void before() {
        Project project = ProjectBuilder.builder().build();
        gatewayConnectionProperties = project.getExtensions().create("GatewayConnection", GatewayConnectionProperties.class, project);
        gatewayConnectionProperties.getUrl().set("");
        gatewayConnectionProperties.getUserName().set("");
        gatewayConnectionProperties.getUserPass().set("");
        buildExportQueryTask = project.getTasks().create("export", BuildExportQueryTask.class);
        buildExportQueryTask.setDocumentTools(DocumentTools.INSTANCE);
        buildExportQueryTask.setGatewayClient(gatewayClient);
        buildExportQueryTask.setGatewayConnectionProperties(gatewayConnectionProperties);
        Mockito.when(gatewayClient.makeGatewayAPICallsWithReturn(Mockito.any(), Mockito.anyString(), Mockito.anyString())).then(invocation -> {
            GatewayClient.APICall apiCall = invocation.getArgument(0);
            return apiCall.apply(httpClient);
        });
    }

    @Test
    public void getFolderIdFromRestmanResponseOk() {
        String singleFolderResponse = "<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:Name>FOLDER List</l7:Name>\n" +
                "    <l7:Type>List</l7:Type>\n" +
                "    <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders?parentFolder.id=0000000000000000ffffffffffffec76&amp;name=gateway%20jam\"/>\n" +
                "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/folders/template\"/>\n" +
                "    <l7:Item>\n" +
                "        <l7:Name>gateway jam</l7:Name>\n" +
                "        <l7:Id>239a94822f0983eb2f8ed699eee082fd</l7:Id>\n" +
                "        <l7:Type>FOLDER</l7:Type>\n" +
                "        <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders/239a94822f0983eb2f8ed699eee082fd\"/>\n" +
                "        <l7:Resource>\n" +
                "            <l7:Folder folderId=\"0000000000000000ffffffffffffec76\" id=\"239a94822f0983eb2f8ed699eee082fd\" version=\"1\">\n" +
                "                <l7:Name>gateway-jam</l7:Name>\n" +
                "            </l7:Folder>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item>\n" +
                "</l7:List>";
        String folderId = buildExportQueryTask.getFolderIdFromRestmanResponse("gateway-jam", "Root Folder", IOUtils.toInputStream(singleFolderResponse, Charset.forName("UTF-8")));

        Assert.assertEquals("239a94822f0983eb2f8ed699eee082fd", folderId);
    }

    @Test(expected = ExportException.class)
    public void getFolderIdFromRestmanResponseEmptyList() {
        String singleFolderResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:Name>FOLDER List</l7:Name>\n" +
                "    <l7:Type>List</l7:Type>\n" +
                "    <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders?parentFolder.id=0000000000000000ffffffffffffec76&amp;name=gateway%20jam\"/>\n" +
                "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/folders/template\"/>\n" +
                "</l7:List>";
        try {
            buildExportQueryTask.getFolderIdFromRestmanResponse("gateway-jam", "Root Folder", IOUtils.toInputStream(singleFolderResponse, Charset.forName("UTF-8")));
        } catch (ExportException e) {
            Assert.assertTrue(e.getMessage().contains("not found"));
            throw e;
        }
    }

    @Test(expected = ExportException.class)
    public void getFolderIdFromRestmanResponseMultipleItems() {
        String singleFolderResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:Name>FOLDER List</l7:Name>\n" +
                "    <l7:Type>List</l7:Type>\n" +
                "    <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders?parentFolder.id=0000000000000000ffffffffffffec76&amp;name=gateway%20jam\"/>\n" +
                "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/folders/template\"/>\n" +
                "    <l7:Item>\n" +
                "        <l7:Name>gateway jam</l7:Name>\n" +
                "        <l7:Id>239a94822f0983eb2f8ed699eee082fd</l7:Id>\n" +
                "        <l7:Type>FOLDER</l7:Type>\n" +
                "        <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders/239a94822f0983eb2f8ed699eee082fd\"/>\n" +
                "        <l7:Resource>\n" +
                "            <l7:Folder folderId=\"0000000000000000ffffffffffffec76\" id=\"239a94822f0983eb2f8ed699eee082fd\" version=\"1\">\n" +
                "                <l7:Name>gateway-jam</l7:Name>\n" +
                "            </l7:Folder>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item>\n" +
                "    <l7:Item>\n" +
                "        <l7:Name>gateway jam</l7:Name>\n" +
                "        <l7:Id>339a94822f0983eb2f8ed699eee082fd</l7:Id>\n" +
                "        <l7:Type>FOLDER</l7:Type>\n" +
                "        <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders/2339a94822f0983eb2f8ed699eee082fd\"/>\n" +
                "        <l7:Resource>\n" +
                "            <l7:Folder folderId=\"0000000000000000ffffffffffffec76\" id=\"339a94822f0983eb2f8ed699eee082fd\" version=\"1\">\n" +
                "                <l7:Name>gateway-jam</l7:Name>\n" +
                "            </l7:Folder>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item>\n" +
                "</l7:List>";
        try {
            buildExportQueryTask.getFolderIdFromRestmanResponse("gateway-jam", "Root Folder", IOUtils.toInputStream(singleFolderResponse, Charset.forName("UTF-8")));
        } catch (ExportException e) {
            Assert.assertTrue(e.getMessage().contains("ultiple"));
            throw e;
        }
    }

    @Test
    public void testGetFolderPathSegments() {
        List<String> folderSegments = BuildExportQueryTask.getFolderPathSegments("/my/folder/path");
        Assert.assertEquals("my", folderSegments.get(0));
        Assert.assertEquals("folder", folderSegments.get(1));
        Assert.assertEquals("path", folderSegments.get(2));

        folderSegments = BuildExportQueryTask.getFolderPathSegments("/");
        Assert.assertTrue(folderSegments.isEmpty());

        boolean caught = false;
        try {
            BuildExportQueryTask.getFolderPathSegments("some/other/path/");
        } catch (ExportException e) {
            Assert.assertTrue(e.getMessage().contains("start"));
            caught = true;
        }
        Assert.assertTrue("Failed to catch expected exception", caught);
    }

    @Test
    public void testPerform() {
        String folder1 = "my";
        String folder1Id = "139a94822f0983eb2f8ed699eee082fd";
        Mockito.when(gatewayClient.makeAPICall(Mockito.any(), Mockito.eq(gatewayConnectionProperties.getUrl().get() + "/1.0/folders?parentFolder.id=" + ROOT_FOLDER_ID + "&name=" + folder1))).thenReturn(IOUtils.toInputStream(buildFolderResponse(folder1, folder1Id, ROOT_FOLDER_ID), Charset.forName("UTF-8")));
        String folder2 = "folder";
        String folder2Id = "239a94822f0983eb2f8ed699eee082fd";
        Mockito.when(gatewayClient.makeAPICall(Mockito.any(), Mockito.eq(gatewayConnectionProperties.getUrl().get() + "/1.0/folders?parentFolder.id=" + folder1Id + "&name=" + folder2))).thenReturn(IOUtils.toInputStream(buildFolderResponse(folder2, folder2Id, folder1Id), Charset.forName("UTF-8")));
        String folder3 = "path";
        String folder3Id = "339a94822f0983eb2f8ed699eee082fd";
        Mockito.when(gatewayClient.makeAPICall(Mockito.any(), Mockito.eq(gatewayConnectionProperties.getUrl().get() + "/1.0/folders?parentFolder.id=" + folder2Id + "&name=" + folder3))).thenReturn(IOUtils.toInputStream(buildFolderResponse(folder3, folder3Id, folder2Id), Charset.forName("UTF-8")));

        String pbsId = "5c012da2ec439a92bfc9f45746ec48bb";
        Mockito.when(gatewayClient.makeAPICall(Mockito.any(), Mockito.eq(gatewayConnectionProperties.getUrl().get() + "/1.0/policyBackedServices"))).thenReturn(IOUtils.toInputStream(buildPBSResponse(pbsId), Charset.forName("UTF-8")));
        String folder4 = "anotherFolder";
        String folder4Id = "5c012da2ec439a92bfc9f45746ec4960";
        Mockito.when(gatewayClient.makeAPICall(Mockito.any(), Mockito.eq(gatewayConnectionProperties.getUrl().get() + "/1.0/folders"))).thenReturn(IOUtils.toInputStream(buildFolderResponse(Arrays.asList(
                new FolderInfo(folder1, folder1Id, ROOT_FOLDER_ID),
                new FolderInfo(folder2, folder2Id, folder1Id),
                new FolderInfo(folder3, folder3Id, folder2Id),
                new FolderInfo(folder4, folder4Id, folder1Id)
        )), Charset.forName("UTF-8")));

        gatewayConnectionProperties.getFolderPath().set("/my/folder/path");
        buildExportQueryTask.perform();

        Assert.assertTrue(buildExportQueryTask.getExportQuery().get().contains("folder=" + ROOT_FOLDER_ID + "&requireFolder=" + ROOT_FOLDER_ID + "&folder=" + folder1Id + "&requireFolder=" + folder1Id + "&folder=" + folder2Id + "&requireFolder=" + folder2Id + "&folder=" + folder3Id));
        Assert.assertFalse(buildExportQueryTask.getExportQuery().get().contains("requireFolder=" + folder3Id));
        Assert.assertTrue(buildExportQueryTask.getExportQuery().get().contains("folder=" + folder4Id + "&requireFolder=" + folder4Id));
        Assert.assertTrue(buildExportQueryTask.getExportQuery().get().contains("requirePolicyBackedService=" + pbsId));
    }

    private String buildPBSResponse(String pbsId) {
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                        "    <l7:Name>POLICY_BACKED_SERVICE List</l7:Name>\n" +
                        "    <l7:Type>List</l7:Type>\n" +
                        "    <l7:TimeStamp>2018-04-24T16:38:02.732Z</l7:TimeStamp>\n" +
                        "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/policyBackedServices\"/>\n" +
                        "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/policyBackedServices/template\"/>\n" +
                        "    <l7:Item>\n" +
                        "        <l7:Name>my-pbs</l7:Name>\n" +
                        "        <l7:Id>%1$s</l7:Id>\n" +
                        "        <l7:Type>POLICY_BACKED_SERVICE</l7:Type>\n" +
                        "        <l7:TimeStamp>2018-04-24T16:38:02.724Z</l7:TimeStamp>\n" +
                        "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/policyBackedServices/5c012da2ec439a92bfc9f45746ec48bb\"/>\n" +
                        "        <l7:Resource>\n" +
                        "            <l7:PolicyBackedService id=\"%1$s\" version=\"0\">\n" +
                        "                <l7:Name>my-pbs</l7:Name>\n" +
                        "                <l7:InterfaceName>com.l7tech.objectmodel.polback.BackgroundTask</l7:InterfaceName>\n" +
                        "                <l7:PolicyBackedServiceOperations>\n" +
                        "                    <l7:PolicyBackedServiceOperation>\n" +
                        "                        <l7:PolicyId>5c012da2ec439a92bfc9f45746ec48a8</l7:PolicyId>\n" +
                        "                        <l7:OperationName>run</l7:OperationName>\n" +
                        "                    </l7:PolicyBackedServiceOperation>\n" +
                        "                </l7:PolicyBackedServiceOperations>\n" +
                        "            </l7:PolicyBackedService>\n" +
                        "        </l7:Resource>\n" +
                        "    </l7:Item>\n" +
                        "</l7:List>\n",
                pbsId);
    }

    private String buildFolderResponse(List<FolderInfo> folders) {
        StringBuilder folderResponse = new StringBuilder();
        folderResponse.append("<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:Name>FOLDER List</l7:Name>\n" +
                "    <l7:Type>List</l7:Type>\n" +
                "    <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders\"/>\n" +
                "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/folders/template\"/>\n");
        folders.forEach(folder -> folderResponse.append(String.format("    <l7:Item>\n" +
                "        <l7:Name>%1$s</l7:Name>\n" +
                "        <l7:Id>%2$s</l7:Id>\n" +
                "        <l7:Type>FOLDER</l7:Type>\n" +
                "        <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders/239a94822f0983eb2f8ed699eee082fd\"/>\n" +
                "        <l7:Resource>\n" +
                "            <l7:Folder folderId=\"%3$s\" id=\"%2$s\" version=\"1\">\n" +
                "                <l7:Name>%1$s</l7:Name>\n" +
                "            </l7:Folder>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item>\n", folder.name, folder.id, folder.parentId)));
        folderResponse.append("</l7:List>");
        return folderResponse.toString();
    }

    private String buildFolderResponse(String folderName, String folderId, String parentFolderId) {
        return String.format("<l7:List xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                        "    <l7:Name>FOLDER List</l7:Name>\n" +
                        "    <l7:Type>List</l7:Type>\n" +
                        "    <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                        "    <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders?parentFolder.id=%3$s&amp;name=%1$s\"/>\n" +
                        "    <l7:Link rel=\"template\" uri=\"https://gateway-dev:8443/restman/1.0/folders/template\"/>\n" +
                        "    <l7:Item>\n" +
                        "        <l7:Name>%1$s</l7:Name>\n" +
                        "        <l7:Id>%2$s</l7:Id>\n" +
                        "        <l7:Type>FOLDER</l7:Type>\n" +
                        "        <l7:TimeStamp>2018-02-28T23:27:08.468Z</l7:TimeStamp>\n" +
                        "        <l7:Link rel=\"self\" uri=\"https://gateway-dev:8443/restman/1.0/folders/239a94822f0983eb2f8ed699eee082fd\"/>\n" +
                        "        <l7:Resource>\n" +
                        "            <l7:Folder folderId=\"%3$s\" id=\"%2$s\" version=\"1\">\n" +
                        "                <l7:Name>%1$s</l7:Name>\n" +
                        "            </l7:Folder>\n" +
                        "        </l7:Resource>\n" +
                        "    </l7:Item>\n" +
                        "</l7:List>",
                folderName, folderId, parentFolderId);
    }

    private class FolderInfo {
        String name;
        String id;
        String parentId;

        private FolderInfo(String name, String id, String parentId) {
            this.name = name;
            this.id = id;
            this.parentId = parentId;
        }
    }
}