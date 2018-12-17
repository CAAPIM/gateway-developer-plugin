/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class EnvironmentBundleCreator {

    private static final Logger logger = Logger.getLogger(EnvironmentBundleCreator.class.getName());

    private DocumentTools documentTools;
    private DocumentFileUtils documentFileUtils;
    private EntityLoaderRegistry entityLoaderRegistry;

    @Inject
    EnvironmentBundleCreator(DocumentTools documentTools, DocumentFileUtils documentFileUtils, EntityLoaderRegistry entityLoaderRegistry) {
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    public Bundle createEnvironmentBundle(Map<String, String> environmentProperties,
                                          String bundleFolderPath,
                                          String templatizedBundleFolderPath,
                                          EnvironmentBundleCreationMode mode,
                                          String bundleFileName) {
        EnvironmentBundleBuilder environmentBundleBuilder = new EnvironmentBundleBuilder(environmentProperties, entityLoaderRegistry);
        Bundle environmentBundle = environmentBundleBuilder.getBundle();

        processDeploymentBundles(environmentBundle, bundleFolderPath, templatizedBundleFolderPath, mode);

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        BundleEntityBuilder bundleEntityBuilder = ConfigBuilderModule.getInjector().getInstance(BundleEntityBuilder.class);

        Element bundleElement = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        documentFileUtils.createFile(bundleElement, new File(bundleFolderPath, bundleFileName).toPath());
        return environmentBundle;
    }

    private void processDeploymentBundles(Bundle environmentBundle, String bundleFolderPath, String templatizedBundleFolderPath, EnvironmentBundleCreationMode mode) {
        File templatizedFolder = new File(templatizedBundleFolderPath);
        File[] templatizedBundles = templatizedFolder.listFiles((dir, name) -> name.endsWith(".bundle"));
        if (templatizedBundles != null) {
            BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
            BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(environmentBundle);
            Arrays.asList(templatizedBundles)
                    .forEach(templatizedBundle -> {
                        logger.log(Level.FINE, () -> "Processing deployment bundle: " + templatizedBundle);
                        String bundleString;
                        try {
                            bundleString = new String(Files.readAllBytes(templatizedBundle.toPath()));
                        } catch (IOException e) {
                            throw new BundleDetemplatizeException("Could not read bundle file: " + templatizedBundle, e);
                        }

                        // detempatize deployment bundles
                        CharSequence detemplatizedBundle = bundleDetemplatizer.detemplatizeBundleString(bundleString);
                        Path bootstrapBundleFilePath = new File(bundleFolderPath, templatizedBundle.getName()).toPath();
                        try {
                            Files.write(bootstrapBundleFilePath, detemplatizedBundle.toString().getBytes());
                        } catch (IOException e) {
                            throw new BundleDetemplatizeException("Could not write detemplatized bundle to: " + bootstrapBundleFilePath, e);
                        }
                        // check deployment bundles to validated that all required environment is provided.
                        bundleEnvironmentValidator.validateEnvironmentProvided(templatizedBundle.getName(), bundleString, mode);
                    });
        }
    }
}
