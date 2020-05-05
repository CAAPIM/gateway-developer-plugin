/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.*;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.FileTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.processDeploymentBundles;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.setTemplatizedBundlesFolderPath;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
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
                                          String environmentConfigurationFolderPath,
                                          EnvironmentBundleCreationMode mode,
                                          String bundleFileName) {
        Bundle environmentBundle = new Bundle();
        environmentBundleBuilder.build(environmentBundle, environmentProperties, environmentConfigurationFolderPath, mode);

        setTemplatizedBundlesFolderPath(templatizedBundleFolderPath);
        processDeploymentBundles(
                environmentBundle,
                collectFiles(templatizedBundleFolderPath, BUNDLE_EXTENSION).stream().map(f -> new FileTemplatizedBundle(f, new File(bundleFolderPath, f.getName()))).collect(toList()),
                mode,
                true);

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final Map<String, Pair<Element, BundleMetadata>> annotatedElements = new LinkedHashMap<>();
        final AnnotatedEntityCreator annotatedEntityCreator = AnnotatedEntityCreator.INSTANCE;
        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        environmentBundle.getEntities(Encass.class).values().stream()
                .filter(Encass::hasAnnotated)
                .map(encass -> annotatedEntityCreator.createAnnotatedEntity(encass, bundleFileName, null))
                .forEach(annotatedEntity -> {
                    if (annotatedEntity.isBundleTypeEnabled()) {
                        // buildEncassDependencies
                        final Pair<Element, BundleMetadata> bundleMetadataPair = bundleEntityBuilder.build(environmentBundle,
                                EntityBuilder.BundleType.ENVIRONMENT, document, bundleFileName, "", null, annotatedEntity);
                        if(bundleMetadataPair != null){
                            annotatedElements.put(annotatedEntity.getBundleName(), bundleMetadataPair);
                        }
                    }
                });

        if(annotatedElements.isEmpty()) {
            annotatedElements.put(bundleFileName, bundleEntityBuilder.build(environmentBundle,
                    EntityBuilder.BundleType.ENVIRONMENT, document, bundleFileName, "", null, null));
        }

        for (Map.Entry<String, Pair<Element, BundleMetadata>> entry : annotatedElements.entrySet()) {
            Pair<Element, BundleMetadata> elementBundleMetadataPair = entry.getValue();
            if(elementBundleMetadataPair != null) {
                documentFileUtils.createFile(elementBundleMetadataPair.getLeft(), new File(bundleFolderPath,
                        entry.getKey()).toPath());
            }
        }
        return environmentBundle;
    }

}
