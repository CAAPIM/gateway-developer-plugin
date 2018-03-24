package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BundleEntityBuilder {

    private final FolderEntityBuilder folderEntityBuilder;
    private final Document document;
    private final ServiceEntityBuilder serviceEntityBuilder;

    public BundleEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        folderEntityBuilder = new FolderEntityBuilder(document, idGenerator);
        serviceEntityBuilder = new ServiceEntityBuilder(document, idGenerator);

    }

    public Element build(Bundle bundle) {
        BundleDocumentBuilder bundleDocumentBuilder = new BundleDocumentBuilder(document);
        bundleDocumentBuilder.addEntities(folderEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(serviceEntityBuilder.build(bundle));

        return bundleDocumentBuilder.build();
    }
}
