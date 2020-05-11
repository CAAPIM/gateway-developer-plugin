/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import org.w3c.dom.Element;

public class BundleArtifacts {

    private final Element deploymentBundle;
    private final Element deleteDeploymentBundle;
    private final BundleMetadata bundleMetadata;

    public BundleArtifacts(Element deploymentBundle, Element deleteDeploymentBundle, BundleMetadata bundleMetadata) {
        this.deploymentBundle = deploymentBundle;
        this.deleteDeploymentBundle = deleteDeploymentBundle;
        this.bundleMetadata = bundleMetadata;
    }

    public Element getDeploymentBundle() {
        return deploymentBundle;
    }

    public Element getDeleteDeploymentBundle() {
        return deleteDeploymentBundle;
    }

    public BundleMetadata getBundleMetadata() {
        return bundleMetadata;
    }
}
