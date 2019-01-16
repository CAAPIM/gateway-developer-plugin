/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.FileTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.StringTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtilsException;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.writeStringToFile;

@Singleton
public class EnvironmentBundleCreator {

    private static final Logger logger = Logger.getLogger(EnvironmentBundleCreator.class.getName());

    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;
    private final EntityBundleLoader entityBundleLoader;
    private final EnvironmentBundleBuilder environmentBundleBuilder;
    private final BundleEntityBuilder bundleEntityBuilder;

    @Inject
    EnvironmentBundleCreator(DocumentTools documentTools,
                             DocumentFileUtils documentFileUtils,
                             EntityBundleLoader entityBundleLoader,
                             EnvironmentBundleBuilder environmentBundleBuilder,
                             BundleEntityBuilder bundleEntityBuilder) {
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        this.entityBundleLoader = entityBundleLoader;
        this.environmentBundleBuilder = environmentBundleBuilder;
        this.bundleEntityBuilder = bundleEntityBuilder;
    }

    public void createFullBundle(Map<String, String> environmentProperties,
                                 List<File> deploymentBundles,
                                 String bundleFolderPath,
                                 String bundleFileName) {
        final String bundle = createFullBundleAsString(environmentProperties, deploymentBundles);
        try {
            writeStringToFile(new File(bundleFolderPath, bundleFileName), bundle, defaultCharset());
        } catch (IOException e) {
            throw new DocumentFileUtilsException("Error writing to file '" + bundleFileName + "': " + e.getMessage(), e);
        }
    }

    public String createFullBundleAsString(Map<String, String> environmentProperties,
                                   List<File> deploymentBundles) {
        Bundle fullBundle = entityBundleLoader.load(deploymentBundles);
        environmentBundleBuilder.build(fullBundle, environmentProperties);

        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        Element bundleElement = bundleEntityBuilder.build(fullBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        String bundleString = documentTools.elementToString(bundleElement);

        // validate and detemplatize
        TemplatizedBundle templatizedBundle = new StringTemplatizedBundle("full-bundle.bundle", bundleString);
        processTemplatizedBundle(templatizedBundle, new BundleEnvironmentValidator(fullBundle), new BundleDetemplatizer(fullBundle), PLUGIN);
        return templatizedBundle.getContents();
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
                stream(templatizedBundles).map(FileTemplatizedBundle::new).collect(toList()),
                mode
        );

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        Element bundleElement = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        documentFileUtils.createFile(bundleElement, new File(bundleFolderPath, bundleFileName).toPath());
        return environmentBundle;
    }

    private void processDeploymentBundles(Bundle environmentBundle,
                                          List<TemplatizedBundle> templatizedBundles,
                                          EnvironmentBundleCreationMode mode) {
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(environmentBundle);
        templatizedBundles.forEach(tb -> processTemplatizedBundle(tb, bundleEnvironmentValidator, bundleDetemplatizer, mode));
    }

    private static void processTemplatizedBundle(TemplatizedBundle templatizedBundle,
                                                 BundleEnvironmentValidator bundleEnvironmentValidator,
                                                 BundleDetemplatizer bundleDetemplatizer,
                                                 EnvironmentBundleCreationMode mode) {
        logger.log(Level.FINE, () -> "Processing deployment bundle: " + templatizedBundle.getName());
        String bundleString = templatizedBundle.getContents();

        // check deployment bundles to validated that all required environment is provided.
        bundleEnvironmentValidator.validateEnvironmentProvided(templatizedBundle.getName(), bundleString, mode);

        // detempatize bundle
        CharSequence detemplatizedBundle = bundleDetemplatizer.detemplatizeBundleString(bundleString);
        templatizedBundle.writeContents(detemplatizedBundle.toString());
    }
}
