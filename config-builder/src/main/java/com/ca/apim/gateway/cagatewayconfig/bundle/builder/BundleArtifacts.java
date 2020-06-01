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
    private final BundleMetadata bundleMetadata;
    private final String bundleFileName;
    private final String deleteBundleFileName;

    public BundleArtifacts(Element bundle, Element deleteBundle, BundleMetadata bundleMetadata, String bundleFileName
            , String deleteBundleFileName) {
        this.bundle = bundle;
        this.deleteBundle = deleteBundle;
        this.bundleMetadata = bundleMetadata;
        this.bundleFileName = bundleFileName;
        this.deleteBundleFileName = deleteBundleFileName;
    }

    public Element getBundle() {
        return bundle;
    }

    public Element getDeleteBundle() {
        return deleteBundle;
    }

    public BundleMetadata getBundleMetadata() {
        return bundleMetadata;
    }

    public String getBundleFileName() {
        return bundleFileName;
    }

    public String getDeleteBundleFileName() {
        return deleteBundleFileName;
    }
}
