/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BundleArtifacts {

    private final Artifact installBundle;
    private final Artifact deleteBundle;
    private final BundleMetadata bundleMetadata;

    // Private Key Import Context XMLs
    private final Set<Artifact> privateKeyContexts = new HashSet<>();

    public BundleArtifacts(Element bundle, Element deleteBundle, BundleMetadata bundleMetadata, String bundleFileName
            , String deleteBundleFileName) {
        this.installBundle = new Artifact(bundle, bundleFileName);
        this.deleteBundle = new Artifact(deleteBundle, deleteBundleFileName);
        this.bundleMetadata = bundleMetadata;
    }

    public Artifact getInstallBundle() {
        return installBundle;
    }

    public Artifact getDeleteBundle() {
        return deleteBundle;
    }

    public BundleMetadata getBundleMetadata() {
        return bundleMetadata;
    }

    public void addPrivateKeyContext(Element contextXml, String filename) {
        privateKeyContexts.add(new Artifact(contextXml, filename));
    }

    public Set<Artifact> getPrivateKeyContexts() {
        return privateKeyContexts;
    }

    public static class Artifact {
        private final Element element;
        private final String filename;

        public Artifact(Element element, String filename) {
            this.element = element;
            this.filename = filename;
        }

        public Element getElement() {
            return element;
        }

        public String getFilename() {
            return filename;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Artifact artifact = (Artifact) o;
            return Objects.equals(filename, artifact.filename);
        }

        @Override
        public int hashCode() {
            return Objects.hash(filename);
        }
    }
}
