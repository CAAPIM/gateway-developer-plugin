package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BundleEntityBuilder {

    private final FolderEntityBuilder folderEntityBuilder;
    private final Document document;
    private final ServiceEntityBuilder serviceEntityBuilder;
    private final PolicyEntityBuilder policyEntityBuilder;

    public BundleEntityBuilder(DocumentFileUtils documentFileUtils, DocumentTools documentTools, Document document, IdGenerator idGenerator) {
        this.document = document;
        folderEntityBuilder = new FolderEntityBuilder(document, idGenerator);
        serviceEntityBuilder = new ServiceEntityBuilder(documentFileUtils, document, idGenerator);
        policyEntityBuilder = new PolicyEntityBuilder(documentFileUtils, documentTools, document, idGenerator);

    }

    public Element build(Bundle bundle) {
        BundleDocumentBuilder bundleDocumentBuilder = new BundleDocumentBuilder(document);
        bundleDocumentBuilder.addEntities(folderEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(policyEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(serviceEntityBuilder.build(bundle));

        return bundleDocumentBuilder.build();
    }
}
