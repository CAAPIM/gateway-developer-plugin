/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EnvironmentBundleBuilder {

    private final Document document;
    private final EnvironmentPropertyEntityBuilder environmentPropertyEntityBuilder;

    public EnvironmentBundleBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        environmentPropertyEntityBuilder = new EnvironmentPropertyEntityBuilder(document, idGenerator);
    }

    public Element build(Bundle bundle) {
        BundleDocumentBuilder bundleDocumentBuilder = new BundleDocumentBuilder(document);
        bundleDocumentBuilder.addEntities(environmentPropertyEntityBuilder.build(bundle));

        return bundleDocumentBuilder.build();
    }
}
