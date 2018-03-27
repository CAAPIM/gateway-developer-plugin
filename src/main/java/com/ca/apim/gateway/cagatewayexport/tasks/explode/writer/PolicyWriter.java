package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.EntityLoaderHelper;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyWriter implements EntityWriter {
    private static final Logger LOGGER = Logger.getLogger(PolicyWriter.class.getName());

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
        Map<String, ServiceEntity> services = bundle.getEntities(ServiceEntity.class);
        services.values().parallelStream().forEach(serviceEntity -> writePolicy(bundle, policyFolder, serviceEntity.getFolderId(), serviceEntity.getName(), serviceEntity.getPolicy()));

        Map<String, PolicyEntity> policies = bundle.getEntities(PolicyEntity.class);
        policies.values().parallelStream().forEach(policyEntity -> writePolicy(bundle, policyFolder, policyEntity.getFolderId(), policyEntity.getName(), policyEntity.getPolicy()));

    }

    private void writePolicy(Bundle bundle, File policyFolder, String folderId, String name, String policy) {
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));

        Path policyPath = folderPath.resolve(name + ".xml");
        try {
            documentFileUtils.createFile(simplifyPolicyXML(WriterHelper.stringToXML(documentTools, policy), bundle), policyPath, false);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception writing policy: " + policyPath + " Message: " + e.getMessage(), e);
        }
    }

    private Element simplifyPolicyXML(Element policyElement, Bundle bundle) {
        NodeList includeReferences = policyElement.getElementsByTagName("L7p:Include");
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new WriteException("Unexpected Include assertion node type: " + includeElement.getNodeType());
            }
            Element policyGuidElement = EntityLoaderHelper.getSingleElement((Element) includeElement, "L7p:PolicyGuid");
            String includedPolicyGuid = policyGuidElement.getAttribute("stringValue");
            Optional<PolicyEntity> policyEntity = bundle.getEntities(PolicyEntity.class).values().stream().filter(p -> includedPolicyGuid.equals(p.getGuid())).findAny();
            if (policyEntity.isPresent()) {
                policyGuidElement.setAttribute("policyPath", getPolicyPath(bundle, policyEntity.get()));
                policyGuidElement.removeAttribute("stringValue");
            } else {
                LOGGER.log(Level.WARNING, "Could not find referenced policy include with guid: %s", includedPolicyGuid);
            }
        }
        return policyElement;
    }

    private String getPolicyPath(Bundle bundle, PolicyEntity policyEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(policyEntity.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policyEntity.getName() + ".xml").toString();
    }
}
