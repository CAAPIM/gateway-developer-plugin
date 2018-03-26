package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class PolicyWriter implements EntityWriter {

    private final DocumentFileUtils documentFileUtils;
    private final DocumentTools documentTools;

    public PolicyWriter(DocumentFileUtils documentFileUtils, DocumentTools documentTools) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File policyFolder = new File(rootFolder, "policy");
        documentFileUtils.createFolder(policyFolder.toPath());

        //create folders
        bundle.getFolderTree().stream().forEach(folder -> {
            if (folder.getParentFolderId() != null) {
                Path folderFile = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
                documentFileUtils.createFolder(folderFile);
            }
        });

        //create policies
        Map<String, Service> services = bundle.getEntities(Service.class);

        services.values().parallelStream().forEach(service -> {
            Folder folder = bundle.getFolderTree().getFolderById(service.getFolderId());
            Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));

            Path policyPath = folderPath.resolve(service.getName() + ".xml");
            try {
                documentFileUtils.createFile(WriterHelper.stringToXML(documentTools, service.getPolicy()), policyPath, false);
            } catch (DocumentParseException e) {
                throw new WriteException("Exception writing policy: " + policyPath + " Message: " + e.getMessage(), e);
            }
        });

    }
}
