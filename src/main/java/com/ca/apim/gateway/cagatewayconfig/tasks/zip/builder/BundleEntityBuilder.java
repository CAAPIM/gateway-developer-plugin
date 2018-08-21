/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

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
    private final EncassEntityBuilder encassEntityBuilder;
    private final ClusterPropertyEntityBuilder clusterPropertyEntityBuilder;
    private final PolicyBackedServiceEntityBuilder policyBackedServiceEntityBuilder;
    private final IdentityProviderEntityBuilder identityProviderEntityBuilder;
    private final ListenPortEntityBuilder listenPortEntityBuilder;

    public BundleEntityBuilder(DocumentFileUtils documentFileUtils, DocumentTools documentTools, Document document, IdGenerator idGenerator) {
        this.document = document;
        folderEntityBuilder = new FolderEntityBuilder(document, idGenerator);
        serviceEntityBuilder = new ServiceEntityBuilder(documentFileUtils, document, idGenerator);
        encassEntityBuilder = new EncassEntityBuilder(document, idGenerator);
        policyEntityBuilder = new PolicyEntityBuilder(documentFileUtils, documentTools, document);
        clusterPropertyEntityBuilder = new ClusterPropertyEntityBuilder(document, idGenerator);
        policyBackedServiceEntityBuilder = new PolicyBackedServiceEntityBuilder(document, idGenerator);
        identityProviderEntityBuilder = new IdentityProviderEntityBuilder(document, idGenerator);
        listenPortEntityBuilder = new ListenPortEntityBuilder(document, idGenerator);
    }

    public Element build(Bundle bundle) {
        BundleDocumentBuilder bundleDocumentBuilder = new BundleDocumentBuilder(document);
        bundleDocumentBuilder.addEntities(folderEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(policyEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(encassEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(serviceEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(clusterPropertyEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(policyBackedServiceEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(identityProviderEntityBuilder.build(bundle));
        bundleDocumentBuilder.addEntities(listenPortEntityBuilder.build(bundle));

        return bundleDocumentBuilder.build();
    }
}
