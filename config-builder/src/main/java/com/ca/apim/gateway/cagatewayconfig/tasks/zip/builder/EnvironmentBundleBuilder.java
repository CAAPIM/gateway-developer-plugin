/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnvironmentBundleBuilder {

    private final EnvironmentPropertyEntityBuilder environmentPropertyEntityBuilder;
    private final BundleDocumentBuilder bundleDocumentBuilder;

    @Inject
    EnvironmentBundleBuilder(final EnvironmentPropertyEntityBuilder environmentPropertyEntityBuilder, final BundleDocumentBuilder bundleDocumentBuilder) {
        this.environmentPropertyEntityBuilder = environmentPropertyEntityBuilder;
        this.bundleDocumentBuilder = bundleDocumentBuilder;
    }

    public Element build(final Bundle bundle, final Document document) {
        return bundleDocumentBuilder.build(document, environmentPropertyEntityBuilder.build(bundle, document));
    }
}
