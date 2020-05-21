/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import org.w3c.dom.Element;

public class BundleArtifacts {

    private final Element bundle;
    private final Element deleteBundle;
    private final Element deleteEnvBundle;
    private final BundleMetadata bundleMetadata;

    public BundleArtifacts(Element bundle, Element deleteBundle, Element deleteEnvBundle, BundleMetadata bundleMetadata) {
        this.bundle = bundle;
        this.deleteBundle = deleteBundle;
        this.deleteEnvBundle = deleteEnvBundle;
        this.bundleMetadata = bundleMetadata;
    }

    public Element getBundle() {
        return bundle;
    }

    public Element getDeleteBundle() {
        return deleteBundle;
    }

    public Element getDeleteEnvBundle() {
        return deleteEnvBundle;
    }

    public BundleMetadata getBundleMetadata() {
        return bundleMetadata;
    }
}
