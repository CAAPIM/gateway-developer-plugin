/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.FileTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.processDeploymentBundles;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Singleton
public class EnvironmentBundleCreator {

    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;
    private final EnvironmentBundleBuilder environmentBundleBuilder;
    private final BundleEntityBuilder bundleEntityBuilder;

    @Inject
    EnvironmentBundleCreator(DocumentTools documentTools,
                             DocumentFileUtils documentFileUtils,
                             EnvironmentBundleBuilder environmentBundleBuilder,
                             BundleEntityBuilder bundleEntityBuilder) {
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        this.environmentBundleBuilder = environmentBundleBuilder;
        this.bundleEntityBuilder = bundleEntityBuilder;
    }

    public Bundle createEnvironmentBundle(Map<String, String> environmentProperties,
                                          String bundleFolderPath,
                                          String templatizedBundleFolderPath,
                                          EnvironmentBundleCreationMode mode,
                                          String bundleFileName) {
        Bundle environmentBundle = new Bundle();
        environmentBundleBuilder.build(environmentBundle, environmentProperties);

        File templatizedFolder = new File(templatizedBundleFolderPath);
        File[] templatizedBundles = ofNullable(templatizedFolder.listFiles((dir, name) -> name.endsWith(".bundle"))).orElse(new File[0]);
        processDeploymentBundles(
                environmentBundle,
                stream(templatizedBundles).map(f -> new FileTemplatizedBundle(f, new File(bundleFolderPath, f.getName()))).collect(toList()),
                mode
        );

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        Element bundleElement = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        documentFileUtils.createFile(bundleElement, new File(bundleFolderPath, bundleFileName).toPath());
        return environmentBundle;
    }

}
