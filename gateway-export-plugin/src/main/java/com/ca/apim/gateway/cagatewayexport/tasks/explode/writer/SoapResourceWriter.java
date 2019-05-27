package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.beans.SoapResource;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.SOAP_RESOURCES_FOLDER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.IOUtils.toInputStream;

/**
 * This class will create wsdl folder under src/main/gateway/. Then it will create folder structure as per gateway under
 * wsdl folder and store wsdl in the same.
 */
@Singleton
public class SoapResourceWriter implements EntityWriter {

    private final DocumentFileUtils documentFileUtils;

    @Inject
    SoapResourceWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        //write wsdl to file
        Map<String, Service> services = bundle.getEntities(Service.class);
        services.values().stream().filter(s -> isNotEmpty(s.getResources())).forEach(serviceEntity -> serviceEntity.getResources().forEach(resource ->
                writeSoapResource(bundle, rootFolder, serviceEntity.getParentFolder().getId(), serviceEntity.getName(), resource)
        ));
    }

    private void writeSoapResource(Bundle bundle, File rootFolder, String folderId, String name, SoapResource resource) {
        File wsdlFolder = new File(rootFolder, SOAP_RESOURCES_FOLDER);
        documentFileUtils.createFolder(wsdlFolder.toPath());
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path folderPath = wsdlFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
        folderPath = new File(folderPath.toFile(), name).toPath();
        documentFileUtils.createFolders(folderPath);

        Path policyPath = folderPath.resolve(resource.getBaseName() + resource.getExtensionByType());
        try (InputStream policyStream = toInputStream(resource.getContent(), UTF_8)) {
            FileUtils.copyInputStreamToFile(policyStream, policyPath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write wsdl to file", e);
        }
    }
}
