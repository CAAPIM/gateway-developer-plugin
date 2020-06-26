/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleArtifacts;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.FileTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.processDeploymentBundles;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.setTemplatizedBundlesFolderPath;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static java.util.stream.Collectors.toList;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
                                          String envInstallBundleFilename) {
        Bundle environmentBundle = new Bundle();
        environmentBundleBuilder.build(environmentBundle, environmentProperties, environmentConfigurationFolderPath, mode);

        setTemplatizedBundlesFolderPath(templatizedBundleFolderPath);
        processDeploymentBundles(
                environmentBundle,
                collectTemplatizedBundleFiles(templatizedBundleFolderPath, mode, bundleFolderPath),
                mode,
                true);

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        // Passing Bundle name and version string with config env name (<name>-<version>-*env.install.bundle) as project name
        Map<String, BundleArtifacts> bundleElements = bundleEntityBuilder.build(environmentBundle,
                EntityBuilder.BundleType.ENVIRONMENT, document, new ProjectInfo(envInstallBundleFilename, EMPTY, EMPTY));
        for (BundleArtifacts bundleArtifacts : bundleElements.values()) {
            documentFileUtils.createFile(bundleArtifacts.getBundle(), new File(bundleFolderPath,
                    bundleArtifacts.getBundleFileName()).toPath());
            documentFileUtils.createFile(bundleArtifacts.getDeleteBundle(), new File(bundleFolderPath,
                    bundleArtifacts.getDeleteBundleFileName()).toPath());
        }
        return environmentBundle;
    }

    private List<TemplatizedBundle> collectTemplatizedBundleFiles(String templatizedBundleFolderPath,
                                                                  EnvironmentBundleCreationMode mode, String bundleFolderPath) {
        final String extension = mode != PLUGIN ? BUNDLE_EXTENSION : "-policy" + INSTALL_BUNDLE_EXTENSION;
        return collectFiles(templatizedBundleFolderPath, extension).stream()
                .filter(file -> !StringUtils.endsWithIgnoreCase(file.getName(), DELETE_BUNDLE_EXTENSION))
                .map(f -> new FileTemplatizedBundle(f, new File(bundleFolderPath, f.getName())))
                .collect(toList());
    }
}